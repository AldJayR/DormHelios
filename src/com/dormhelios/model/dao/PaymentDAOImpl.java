package com.dormhelios.model.dao;

import com.dormhelios.model.entity.Payment;
import com.dormhelios.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.math.BigDecimal;

public class PaymentDAOImpl implements PaymentDAO {

    private static final Logger LOGGER = Logger.getLogger(PaymentDAOImpl.class.getName());

    // --- SQL Constants ---
    private static final String FIND_BY_ID_SQL = "SELECT * FROM payments WHERE payment_id = ?";
    private static final String FIND_ALL_SQL = "SELECT * FROM payments ORDER BY payment_date DESC, created_at DESC";
    private static final String FIND_BY_TENANT_ID_SQL = "SELECT * FROM payments WHERE tenant_id = ? ORDER BY payment_date DESC, created_at DESC";
    private static final String FIND_BY_DATE_RANGE_SQL = "SELECT * FROM payments WHERE payment_date BETWEEN ? AND ? ORDER BY payment_date DESC, tenant_id";
    private static final String FIND_BY_USER_ID_SQL = "SELECT * FROM payments WHERE user_id = ? ORDER BY created_at DESC";
    private static final String ADD_SQL = "INSERT INTO payments (tenant_id, user_id, payment_date, amount, payment_method, period_covered_start, period_covered_end, receipt_reference, qr_code_data, notes, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
    private static final String UPDATE_SQL = "UPDATE payments SET tenant_id = ?, user_id = ?, payment_date = ?, amount = ?, payment_method = ?, period_covered_start = ?, period_covered_end = ?, receipt_reference = ?, qr_code_data = ?, notes = ? WHERE payment_id = ?"; // Note: created_at usually not updated
    private static final String DELETE_SQL = "DELETE FROM payments WHERE payment_id = ?";
    private static final String SUM_AMOUNT_BY_DATE_RANGE_SQL = "SELECT SUM(amount) FROM payments WHERE payment_date BETWEEN ? AND ?"; // New SQL

    // Overdue payments SQL - Tenants who haven't paid in current month
    private static final String FIND_OVERDUE_PAYMENTS_SQL = 
        "SELECT t.id, t.first_name, t.last_name, r.room_number, MAX(p.payment_date) as last_payment_date " +
        "FROM tenants t " +
        "LEFT JOIN rooms r ON t.room_id = r.id " +
        "LEFT JOIN payments p ON t.id = p.tenant_id " +
        "WHERE t.is_active = TRUE " +
        "GROUP BY t.id, t.first_name, t.last_name, r.room_number " +
        "HAVING MAX(p.payment_date) < ? OR MAX(p.payment_date) IS NULL " +
        "ORDER BY last_payment_date ASC, t.last_name, t.first_name";
    
    // Recent payments SQL for dashboard
    private static final String FIND_RECENT_PAYMENTS_SQL = 
        "SELECT p.*, t.first_name, t.last_name, r.room_number " +
        "FROM payments p " +
        "JOIN tenants t ON p.tenant_id = t.id " +
        "LEFT JOIN rooms r ON t.room_id = r.id " +
        "WHERE p.payment_date BETWEEN ? AND ? " +
        "ORDER BY p.payment_date DESC, p.created_at DESC " +
        "LIMIT ?";

    @Override
    public Optional<Payment> findById(int paymentId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(FIND_BY_ID_SQL)) {
            pstmt.setInt(1, paymentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding payment by ID: " + paymentId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Payment> findAll() {
        List<Payment> payments = new ArrayList<>();
        LOGGER.log(Level.INFO, "Attempting to retrieve all payments from database...");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = pstmt.executeQuery()) {
            
            int count = 0;
            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
                count++;
            }
            LOGGER.log(Level.INFO, "Successfully retrieved {0} payments from database", count);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all payments", e);
        }
        return payments;
    }

    @Override
    public List<Payment> findByTenantId(int tenantId) {
        List<Payment> payments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(FIND_BY_TENANT_ID_SQL)) {
            pstmt.setInt(1, tenantId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding payments for tenant ID: " + tenantId, e);
        }
        return payments;
    }

    @Override
    public List<Payment> findByDateRange(LocalDate startDate, LocalDate endDate) {
         List<Payment> payments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(FIND_BY_DATE_RANGE_SQL)) {
            // Convert LocalDate to java.sql.Date for PreparedStatement
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding payments between " + startDate + " and " + endDate, e);
        }
        return payments;
    }

     @Override
    public List<Payment> findByUserId(int userId) {
        List<Payment> payments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(FIND_BY_USER_ID_SQL)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding payments logged by user ID: " + userId, e);
        }
        return payments;
    }


    @Override
    public int addPayment(Payment payment) {
        ResultSet generatedKeys = null;
        // NOTE: In a real application, adding a payment might be part of a larger transaction
        // (e.g., updating tenant balance) requiring conn.setAutoCommit(false), commit(), rollback().
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(ADD_SQL, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, payment.getTenantId());
            pstmt.setInt(2, payment.getUserId()); // Who logged it
            pstmt.setDate(3, Date.valueOf(payment.getPaymentDate()));
            pstmt.setBigDecimal(4, payment.getAmount());
            pstmt.setString(5, payment.getPaymentMethod().name()); // Store Enum name
            pstmt.setObject(6, payment.getPeriodCoveredStart() != null ? Date.valueOf(payment.getPeriodCoveredStart()) : null);
            pstmt.setObject(7, payment.getPeriodCoveredEnd() != null ? Date.valueOf(payment.getPeriodCoveredEnd()) : null);
            pstmt.setString(8, payment.getReceiptReference());
            pstmt.setString(9, payment.getQrCodeData());
            pstmt.setString(10, payment.getNotes());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Return new payment_id
                } else {
                    LOGGER.log(Level.WARNING, "Failed to retrieve generated key for new payment.");
                    return -1;
                }
            } else {
                return -1;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding payment for tenant ID: " + payment.getTenantId(), e);
            return -1;
        } finally {
             if (generatedKeys != null) {
                 try { generatedKeys.close(); } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error closing generated keys ResultSet", e); }
             }
        }
    }

    @Override
    public boolean updatePayment(Payment payment) {
        // Updating payments is often restricted or heavily logged for auditing.
        // Ensure business logic allows this before implementing fully.
        if (payment.getPaymentId() <= 0) {
            LOGGER.log(Level.WARNING, "Attempted to update payment with invalid ID: 0");
            return false;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_SQL)) {

            pstmt.setInt(1, payment.getTenantId());
            pstmt.setInt(2, payment.getUserId()); // Maybe update who last modified it? Or keep original logger?
            pstmt.setDate(3, Date.valueOf(payment.getPaymentDate()));
            pstmt.setBigDecimal(4, payment.getAmount());
            pstmt.setString(5, payment.getPaymentMethod().name());
            pstmt.setObject(6, payment.getPeriodCoveredStart() != null ? Date.valueOf(payment.getPeriodCoveredStart()) : null);
            pstmt.setObject(7, payment.getPeriodCoveredEnd() != null ? Date.valueOf(payment.getPeriodCoveredEnd()) : null);
            pstmt.setString(8, payment.getReceiptReference());
            pstmt.setString(9, payment.getQrCodeData());
            pstmt.setString(10, payment.getNotes());
            // WHERE clause
            pstmt.setInt(11, payment.getPaymentId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating payment: " + payment.getPaymentId(), e);
            return false;
        }
    }

    @Override
    public boolean deletePayment(int paymentId) {
        // Deleting financial records is HIGHLY discouraged.
        // Consider adding an 'is_void' or 'is_cancelled' flag instead (soft delete).
        LOGGER.log(Level.WARNING, "Attempting to permanently delete payment record with ID: " + paymentId + ". This is generally discouraged.");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_SQL)) {

            pstmt.setInt(1, paymentId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting payment: " + paymentId, e);
            return false;
        }
    }

    @Override
    public BigDecimal sumAmountByDateRange(LocalDate startDate, LocalDate endDate) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SUM_AMOUNT_BY_DATE_RANGE_SQL)) {
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal sum = rs.getBigDecimal(1);
                    return sum != null ? sum : BigDecimal.ZERO; // Return sum or ZERO if no payments found
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error summing payment amounts between " + startDate + " and " + endDate, e);
        }
        return BigDecimal.ZERO; // Return ZERO on error
    }

    @Override
    public List<Payment> findOverduePayments() {
        List<Payment> overduePayments = new ArrayList<>();
        LocalDate firstDayOfCurrentMonth = LocalDate.now().withDayOfMonth(1);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(FIND_OVERDUE_PAYMENTS_SQL)) {
            
            pstmt.setDate(1, Date.valueOf(firstDayOfCurrentMonth));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Payment payment = new Payment();
                    payment.setPaymentId(-1); // Placeholder ID
                    payment.setTenantId(rs.getInt("id"));
                    
                    // Create tenant and room objects to attach to payment
                    payment.setTenant(createTenantForOverduePayment(rs));
                    payment.setRoom(createRoomForOverduePayment(rs));
                    
                    Date lastPaymentDate = rs.getDate("last_payment_date");
                    if (lastPaymentDate != null) {
                        payment.setPaymentDate(lastPaymentDate.toLocalDate());
                    } else {
                        payment.setPaymentDate(LocalDate.now().minusMonths(1)); // Default to previous month
                    }
                    
                    overduePayments.add(payment);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding overdue payments", e);
        }
        
        return overduePayments;
    }
    
    @Override
    public List<Payment> findRecentPayments(int limit) {
        List<Payment> recentPayments = new ArrayList<>();
        // Get payments from the last 30 days by default
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        LocalDate today = LocalDate.now();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(FIND_RECENT_PAYMENTS_SQL)) {
            
            pstmt.setDate(1, Date.valueOf(thirtyDaysAgo));
            pstmt.setDate(2, Date.valueOf(today));
            pstmt.setInt(3, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Payment payment = mapResultSetToPayment(rs);
                    
                    // Add tenant and room information
                    payment.setTenant(createTenantForRecentPayment(rs));
                    payment.setRoom(createRoomForRecentPayment(rs));
                    
                    recentPayments.add(payment);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding recent payments (limit: " + limit + ")", e);
        }
        
        return recentPayments;
    }
    
    // Helper methods to create tenant and room objects for dashboard display
    private com.dormhelios.model.entity.Tenant createTenantForOverduePayment(ResultSet rs) throws SQLException {
        com.dormhelios.model.entity.Tenant tenant = new com.dormhelios.model.entity.Tenant();
        tenant.setTenantId(rs.getInt("id"));
        tenant.setFirstName(rs.getString("first_name"));
        tenant.setLastName(rs.getString("last_name"));
        return tenant;
    }
    
    private com.dormhelios.model.entity.Room createRoomForOverduePayment(ResultSet rs) throws SQLException {
        com.dormhelios.model.entity.Room room = new com.dormhelios.model.entity.Room();
        room.setRoomNumber(rs.getString("room_number"));
        return room;
    }
    
    private com.dormhelios.model.entity.Tenant createTenantForRecentPayment(ResultSet rs) throws SQLException {
        com.dormhelios.model.entity.Tenant tenant = new com.dormhelios.model.entity.Tenant();
        tenant.setTenantId(rs.getInt("tenant_id"));
        tenant.setFirstName(rs.getString("first_name"));
        tenant.setLastName(rs.getString("last_name"));
        return tenant;
    }
    
    private com.dormhelios.model.entity.Room createRoomForRecentPayment(ResultSet rs) throws SQLException {
        com.dormhelios.model.entity.Room room = new com.dormhelios.model.entity.Room();
        room.setRoomNumber(rs.getString("room_number"));
        return room;
    }

    // --- Helper Method for Mapping ---

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setTenantId(rs.getInt("tenant_id"));
        payment.setUserId(rs.getInt("user_id"));
        payment.setPaymentDate(rs.getDate("payment_date").toLocalDate());
        payment.setAmount(rs.getBigDecimal("amount"));

        // Handle Enum
        try {
            payment.setPaymentMethod(Payment.PaymentMethod.valueOf(rs.getString("payment_method").toUpperCase()));
        } catch (IllegalArgumentException | NullPointerException e) {
            LOGGER.log(Level.WARNING, "Invalid or NULL payment method found in DB for payment ID: " + payment.getPaymentId() + ". Defaulting to OTHER.");
            payment.setPaymentMethod(Payment.PaymentMethod.OTHER); // Default fallback
        }

        // Handle nullable LocalDate
        Date periodStartDb = rs.getDate("period_covered_start");
        payment.setPeriodCoveredStart(periodStartDb != null ? periodStartDb.toLocalDate() : null);
        Date periodEndDb = rs.getDate("period_covered_end");
        payment.setPeriodCoveredEnd(periodEndDb != null ? periodEndDb.toLocalDate() : null);

        payment.setReceiptReference(rs.getString("receipt_reference"));
        payment.setQrCodeData(rs.getString("qr_code_data"));
        payment.setNotes(rs.getString("notes"));
        payment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        return payment;
    }
}