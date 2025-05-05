package com.dormhelios.model.dao;

import com.dormhelios.model.entity.Tenant;
import com.dormhelios.model.entity.TenantWithRoom; // Assuming this entity exists
import com.dormhelios.util.DatabaseConnection; // Assumes this utility provides connections

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TenantDAOImpl implements TenantDAO {

    private static final Logger LOGGER = Logger.getLogger(TenantDAOImpl.class.getName());

    // --- SQL Constants ---
    private static final String FIND_BY_ID_SQL = "SELECT * FROM TENANTS WHERE id = ?";
    private static final String FIND_ALL_SQL = "SELECT * FROM TENANTS ORDER BY last_name, first_name";
    private static final String FIND_BY_ROOM_ID_SQL = "SELECT * FROM TENANTS WHERE room_id = ? ORDER BY last_name, first_name";
    private static final String FIND_BY_LAST_NAME_SQL = "SELECT * FROM TENANTS WHERE last_name LIKE ? ORDER BY first_name";
    private static final String ADD_SQL = 
        "INSERT INTO TENANTS (user_id, room_id, guardian_id, emergency_contact_id, first_name, last_name, student_number, email, phone_number, permanent_address, lease_start_date, lease_end_date, deposit_amount, deposit_status, created_at, updated_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
    private static final String UPDATE_SQL = 
        "UPDATE TENANTS SET user_id = ?, room_id = ?, guardian_id = ?, emergency_contact_id = ?, first_name = ?, last_name = ?, student_number = ?, email = ?, phone_number = ?, permanent_address = ?, lease_start_date = ?, lease_end_date = ?, deposit_amount = ?, deposit_status = ?, notes = ?, updated_at = NOW() " +
        "WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM TENANTS WHERE id = ?";
    private static final String ASSIGN_ROOM_SQL = "UPDATE TENANTS SET room_id = ?, updated_at = NOW() WHERE id = ?";
    private static final String ASSIGN_GUARDIAN_SQL = "UPDATE TENANTS SET guardian_id = ?, updated_at = NOW() WHERE id = ?";
    private static final String ASSIGN_EMERGENCY_CONTACT_SQL = "UPDATE TENANTS SET emergency_contact_id = ?, updated_at = NOW() WHERE id = ?";
    private static final String ASSIGN_USER_ACCOUNT_SQL = "UPDATE TENANTS SET user_id = ?, updated_at = NOW() WHERE id = ?";
    private static final String COUNT_ALL_SQL = "SELECT COUNT(*) FROM TENANTS WHERE is_active = TRUE"; // Filter active
    private static final String SET_ACTIVE_STATUS_SQL = "UPDATE TENANTS SET is_active = ?, updated_at = NOW() WHERE id = ?"; // New SQL for soft delete
    private static final String FIND_ALL_WITH_ROOM_NUMBERS_SQL = 
        "SELECT t.*, r.room_number FROM TENANTS t " +
        "LEFT JOIN ROOMS r ON t.room_id = r.id " +
        "WHERE t.is_active = 1 " +
        "ORDER BY t.last_name, t.first_name";

    // --- DAO Methods ---
    @Override
    public Optional<Tenant> findById(int tenantId) {
        final String sql = FIND_BY_ID_SQL + " AND is_active = TRUE";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tenantId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTenant(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding active tenant by ID: " + tenantId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Tenant> findAll() {
        List<Tenant> tenants = new ArrayList<>();
        final String sql = FIND_ALL_SQL.replace("ORDER BY", "WHERE is_active = 1 ORDER BY");
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                tenants.add(mapResultSetToTenant(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all active tenants", e);
        }
        return tenants;
    }

    @Override
    public List<Tenant> findAllIncludingInactive() {
        List<Tenant> tenants = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(FIND_ALL_SQL); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                tenants.add(mapResultSetToTenant(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all tenants (including inactive)", e);
        }
        return tenants;
    }

    @Override
    public List<Tenant> findByRoomId(int roomId) {
        List<Tenant> tenants = new ArrayList<>();
        final String sql = FIND_BY_ROOM_ID_SQL.replace("ORDER BY", "AND is_active = TRUE ORDER BY");
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tenants.add(mapResultSetToTenant(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding active tenants by Room ID: " + roomId, e);
        }
        return tenants;
    }

    @Override
    public List<Tenant> findByLastName(String lastName) {
        List<Tenant> tenants = new ArrayList<>();
        final String sql = FIND_BY_LAST_NAME_SQL.replace("ORDER BY", "AND is_active = TRUE ORDER BY");
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, lastName + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tenants.add(mapResultSetToTenant(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding active tenant by last name: " + lastName, e);
        }
        return tenants;
    }

    @Override
    public int addTenant(Tenant tenant) {
        ResultSet generatedKeys = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean shouldCommit = false;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            pstmt = conn.prepareStatement(ADD_SQL, Statement.RETURN_GENERATED_KEYS);

            pstmt.setObject(1, tenant.getUserId());
            pstmt.setObject(2, tenant.getRoomId());
            pstmt.setObject(3, tenant.getGuardianId());
            pstmt.setObject(4, tenant.getEmergencyContactId());
            pstmt.setString(5, tenant.getFirstName());
            pstmt.setString(6, tenant.getLastName());
            pstmt.setString(7, tenant.getStudentIdNumber());
            pstmt.setString(8, tenant.getEmail());
            pstmt.setString(9, tenant.getPhoneNumber());
            pstmt.setString(10, tenant.getPermanentAddress());
            pstmt.setObject(11, tenant.getLeaseStartDate() != null ? Date.valueOf(tenant.getLeaseStartDate()) : null);
            pstmt.setObject(12, tenant.getLeaseEndDate() != null ? Date.valueOf(tenant.getLeaseEndDate()) : null);
            pstmt.setBigDecimal(13, tenant.getSecurityDepositAmount());
            pstmt.setString(14, tenant.getSecurityDepositStatus().name());

            int affectedRows = pstmt.executeUpdate();
            int tenantId = -1;

            if (affectedRows > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    tenantId = generatedKeys.getInt(1);
                    
                    // If a room is assigned, decrement slots_available
                    if (tenant.getRoomId() != null && tenant.getRoomId() > 0) {
                        RoomDAOImpl roomDAO = new RoomDAOImpl();
                        if (!roomDAO.decrementSlotsAvailable(tenant.getRoomId())) {
                            LOGGER.log(Level.WARNING, "Could not decrement slots_available for room: " + tenant.getRoomId());
                            // If we can't decrement slots (e.g., room is full), roll back transaction
                            conn.rollback();
                            return -1;
                        }
                    }
                    
                    shouldCommit = true;
                    return tenantId;
                } else {
                    LOGGER.log(Level.WARNING, "Failed to retrieve generated key for new tenant.");
                }
            }
            
            return -1;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding tenant: " + tenant.getFirstName() + " " + tenant.getLastName(), e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
            }
            return -1;
        } finally {
            if (generatedKeys != null) {
                try {
                    generatedKeys.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error closing generated keys ResultSet", e);
                }
            }
            
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error closing prepared statement", e);
                }
            }
            
            if (conn != null) {
                try {
                    if (shouldCommit) {
                        conn.commit();
                    }
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error committing transaction or closing connection", e);
                }
            }
        }
    }

    @Override
    public boolean updateTenant(Tenant tenant) {
        if (tenant.getTenantId() <= 0) {
            LOGGER.log(Level.WARNING, "Attempted to update tenant with invalid ID: 0");
            return false;
        }
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(UPDATE_SQL)) {

            pstmt.setObject(1, tenant.getUserId());
            pstmt.setObject(2, tenant.getRoomId());
            pstmt.setObject(3, tenant.getGuardianId());
            pstmt.setObject(4, tenant.getEmergencyContactId());
            pstmt.setString(5, tenant.getFirstName());
            pstmt.setString(6, tenant.getLastName());
            pstmt.setString(7, tenant.getStudentIdNumber());
            pstmt.setString(8, tenant.getEmail());
            pstmt.setString(9, tenant.getPhoneNumber());
            pstmt.setString(10, tenant.getPermanentAddress());
            pstmt.setObject(11, tenant.getLeaseStartDate() != null ? Date.valueOf(tenant.getLeaseStartDate()) : null);
            pstmt.setObject(12, tenant.getLeaseEndDate() != null ? Date.valueOf(tenant.getLeaseEndDate()) : null);
            pstmt.setBigDecimal(13, tenant.getSecurityDepositAmount());
            pstmt.setString(14, tenant.getSecurityDepositStatus().name());
            pstmt.setString(15, tenant.getNotes());
            pstmt.setInt(16, tenant.getTenantId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating tenant: " + tenant.getTenantId(), e);
            return false;
        }
    }

    @Override
    public int countAll() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(COUNT_ALL_SQL);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting all active tenants", e);
        }
        return 0;
    }

    @Override
    public boolean setActiveStatus(int tenantId, boolean isActive) {
        LOGGER.log(Level.INFO, "Setting active status to {0} for tenant ID: {1}", new Object[]{isActive, tenantId});
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SET_ACTIVE_STATUS_SQL)) {
            pstmt.setBoolean(1, isActive);
            pstmt.setInt(2, tenantId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error setting active status for tenant: " + tenantId, e);
            return false;
        }
    }

    @Override
    public List<TenantWithRoom> findAllWithRoomNumbers() {
        List<TenantWithRoom> tenantsWithRooms = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(FIND_ALL_WITH_ROOM_NUMBERS_SQL); 
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Tenant tenant = mapResultSetToTenant(rs);
                String roomNumber = rs.getString("room_number"); // Get room number from joined table
                tenantsWithRooms.add(new TenantWithRoom(tenant, roomNumber));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all active tenants with room numbers", e);
        }
        return tenantsWithRooms;
    }

    @Override
    public int countNewTenantsByDateRange(LocalDate startDate, LocalDate endDate) {
        final String sql = "SELECT COUNT(*) FROM TENANTS WHERE created_at BETWEEN ? AND ? AND is_active = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Add one day to endDate to make it inclusive up to the end of the day
            java.sql.Timestamp startTs = java.sql.Timestamp.valueOf(startDate.atStartOfDay());
            java.sql.Timestamp endTs = java.sql.Timestamp.valueOf(endDate.plusDays(1).atStartOfDay());
            
            pstmt.setTimestamp(1, startTs);
            pstmt.setTimestamp(2, endTs);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting new tenants for date range: " + startDate + " to " + endDate, e);
        }
        return 0;
    }

    // --- Helper methods for specific assignments ---
    private boolean updateTenantForeignKey(String sql, int tenantId, Integer foreignKeyId) {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, foreignKeyId);
            pstmt.setInt(2, tenantId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating foreign key for tenant: " + tenantId + " using SQL: " + sql, e);
            return false;
        }
    }

    @Override
    public boolean assignTenantToRoom(int tenantId, Integer roomId) {
        // Get the current tenant data to check if they're already assigned to a room
        Optional<Tenant> currentTenant = findById(tenantId);
        if (!currentTenant.isPresent()) {
            LOGGER.log(Level.WARNING, "Cannot assign room: Tenant not found with ID: " + tenantId);
            return false;
        }
        
        Integer oldRoomId = currentTenant.get().getRoomId();
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Update tenant's room assignment
            pstmt = conn.prepareStatement(ASSIGN_ROOM_SQL);
            pstmt.setObject(1, roomId); // Can be null to unassign
            pstmt.setInt(2, tenantId);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows <= 0) {
                conn.rollback();
                return false;
            }
            
            // Handle slots_available updates
            RoomDAOImpl roomDAO = new RoomDAOImpl();
            
            // If tenant was previously assigned to a room, increment that room's slots_available
            if (oldRoomId != null && oldRoomId > 0) {
                if (!roomDAO.incrementSlotsAvailable(oldRoomId)) {
                    LOGGER.log(Level.WARNING, "Failed to increment slots_available for previous room: " + oldRoomId);
                    conn.rollback();
                    return false;
                }
            }
            
            // If tenant is being assigned to a new room, decrement that room's slots_available
            if (roomId != null && roomId > 0) {
                if (!roomDAO.decrementSlotsAvailable(roomId)) {
                    LOGGER.log(Level.WARNING, "Failed to decrement slots_available for new room: " + roomId);
                    conn.rollback();
                    return false;
                }
            }
            
            conn.commit();
            success = true;
            return true;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error assigning tenant " + tenantId + " to room " + roomId, e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
            }
            return false;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error closing prepared statement", e);
                }
            }
            
            if (conn != null) {
                try {
                    if (!success) {
                        conn.rollback();
                    }
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error closing connection", e);
                }
            }
        }
    }

    @Override
    public boolean assignGuardianToTenant(int tenantId, Integer guardianId) {
        return updateTenantForeignKey(ASSIGN_GUARDIAN_SQL, tenantId, guardianId);
    }

    @Override
    public boolean assignEmergencyContactToTenant(int tenantId, Integer contactId) {
        return updateTenantForeignKey(ASSIGN_EMERGENCY_CONTACT_SQL, tenantId, contactId);
    }

    @Override
    public boolean assignUserAccountToTenant(int tenantId, Integer userId) {
        return updateTenantForeignKey(ASSIGN_USER_ACCOUNT_SQL, tenantId, userId);
    }

    // --- Helper Method for Mapping ---
    private Tenant mapResultSetToTenant(ResultSet rs) throws SQLException {
        Tenant tenant = new Tenant();
        tenant.setTenantId(rs.getInt("id"));

        tenant.setUserId((Integer) rs.getObject("user_id"));
        tenant.setRoomId((Integer) rs.getObject("room_id"));
        tenant.setGuardianId((Integer) rs.getObject("guardian_id"));
        tenant.setEmergencyContactId((Integer) rs.getObject("emergency_contact_id"));

        tenant.setFirstName(rs.getString("first_name"));
        tenant.setLastName(rs.getString("last_name"));
        tenant.setStudentIdNumber(rs.getString("student_number"));
        tenant.setEmail(rs.getString("email"));
        tenant.setPhoneNumber(rs.getString("phone_number"));
        tenant.setPermanentAddress(rs.getString("permanent_address"));

        Date leaseStartDateDb = rs.getDate("lease_start_date");
        tenant.setLeaseStartDate(leaseStartDateDb != null ? leaseStartDateDb.toLocalDate() : null);
        Date leaseEndDateDb = rs.getDate("lease_end_date");
        tenant.setLeaseEndDate(leaseEndDateDb != null ? leaseEndDateDb.toLocalDate() : null);

        tenant.setSecurityDepositAmount(rs.getBigDecimal("deposit_amount"));

        try {
            tenant.setSecurityDepositStatus(Tenant.DepositStatus.valueOf(rs.getString("deposit_status").toUpperCase()));
        } catch (IllegalArgumentException | NullPointerException e) {
            LOGGER.log(Level.WARNING, "Invalid or NULL security deposit status found in DB for tenant ID: " + tenant.getTenantId() + ". Defaulting to PENDING.");
            tenant.setSecurityDepositStatus(Tenant.DepositStatus.PENDING);
        }

        tenant.setNotes(rs.getString("notes"));

        tenant.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        tenant.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        tenant.setActive(rs.getBoolean("is_active"));

        return tenant;
    }
}
