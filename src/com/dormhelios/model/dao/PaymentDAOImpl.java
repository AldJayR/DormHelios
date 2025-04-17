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
    private static final String FIND_BY_ID_SQL = "SELECT * FROM PAYMENTS WHERE payment_id = ?";
    private static final String FIND_ALL_SQL = "SELECT * FROM PAYMENTS ORDER BY payment_date DESC, created_at DESC";
    private static final String FIND_BY_TENANT_ID_SQL = "SELECT * FROM PAYMENTS WHERE tenant_id = ? ORDER BY payment_date DESC, created_at DESC";
    private static final String FIND_BY_DATE_RANGE_SQL = "SELECT * FROM PAYMENTS WHERE payment_date BETWEEN ? AND ? ORDER BY payment_date DESC, tenant_id";
    private static final String FIND_BY_USER_ID_SQL = "SELECT * FROM PAYMENTS WHERE user_id = ? ORDER BY created_at DESC";
    private static final String ADD_SQL = "INSERT INTO PAYMENTS (tenant_id, user_id, payment_date, amount, payment_method, period_covered_start, period_covered_end, receipt_reference, qr_code_data, notes, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
    private static final String UPDATE_SQL = "UPDATE PAYMENTS SET tenant_id = ?, user_id = ?, payment_date = ?, amount = ?, payment_method = ?, period_covered_start = ?, period_covered_end = ?, receipt_reference = ?, qr_code_data = ?, notes = ? WHERE payment_id = ?"; // Note: created_at usually not updated
    private static final String DELETE_SQL = "DELETE FROM PAYMENTS WHERE payment_id = ?";
    private static final String SUM_AMOUNT_BY_DATE_RANGE_SQL = "SELECT SUM(amount) FROM PAYMENTS WHERE payment_date BETWEEN ? AND ?"; // New SQL


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
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
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