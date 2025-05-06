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
import java.util.Objects;

public class TenantDAOImpl implements TenantDAO {

    private static final Logger LOGGER = Logger.getLogger(TenantDAOImpl.class.getName());

    // --- SQL Constants ---
    private static final String FIND_BY_ID_SQL = "SELECT * FROM TENANTS WHERE id = ?";
    private static final String FIND_ALL_SQL = "SELECT * FROM TENANTS ORDER BY last_name, first_name";
    private static final String FIND_BY_ROOM_ID_SQL = "SELECT * FROM TENANTS WHERE room_id = ? ORDER BY last_name, first_name";
    private static final String FIND_BY_LAST_NAME_SQL = "SELECT * FROM TENANTS WHERE last_name LIKE ? ORDER BY first_name";
    private static final String ADD_SQL = 
        "INSERT INTO TENANTS (user_id, room_id, guardian_name, emergency_contact_number, first_name, last_name, student_number, email, phone_number, permanent_address, lease_start_date, lease_end_date, deposit_amount, deposit_status, created_at, updated_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
    private static final String UPDATE_SQL = 
        "UPDATE TENANTS SET user_id = ?, room_id = ?, guardian_name = ?, emergency_contact_number = ?, first_name = ?, last_name = ?, student_number = ?, email = ?, phone_number = ?, permanent_address = ?, lease_start_date = ?, lease_end_date = ?, deposit_amount = ?, deposit_status = ?, notes = ?, updated_at = NOW() " +
        "WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM TENANTS WHERE id = ?";
    private static final String ASSIGN_ROOM_SQL = "UPDATE TENANTS SET room_id = ?, updated_at = NOW() WHERE id = ?";
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
        RoomDAOImpl roomDAO = new RoomDAOImpl(); // Instantiate DAO once

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            pstmt = conn.prepareStatement(ADD_SQL, Statement.RETURN_GENERATED_KEYS);

            pstmt.setObject(1, tenant.getUserId());
            pstmt.setObject(2, tenant.getRoomId());
            pstmt.setString(3, tenant.getGuardianName());
            pstmt.setString(4, tenant.getEmergencyContactNumber());
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

                    // If a room is assigned, decrement slots_available using the SAME transaction
                    if (tenant.getRoomId() != null && tenant.getRoomId() > 0) {
                        // Use the transactional method with the existing connection
                        if (!roomDAO.decrementSlotsAvailable(tenant.getRoomId(), conn)) {
                            // If decrement fails (e.g., no slots), log and trigger rollback
                            LOGGER.log(Level.WARNING, "(Tx) Could not decrement slots_available for room: " + tenant.getRoomId() + ". Rolling back tenant addition.");
                            throw new SQLException("Failed to decrement room slots, tenant addition rolled back."); // Throw exception to trigger rollback
                        }
                        LOGGER.log(Level.INFO, "(Tx) Decremented slots for room {0} during tenant {1} addition.", new Object[]{tenant.getRoomId(), tenantId});
                    }

                    shouldCommit = true; // Mark for commit only if all steps succeed
                    conn.commit(); // Commit the transaction
                    LOGGER.log(Level.INFO, "Successfully added tenant ID: {0} and updated room slots. Transaction committed.", tenantId);
                    return tenantId;
                } else {
                    LOGGER.log(Level.WARNING, "Failed to retrieve generated key for new tenant. Rolling back.");
                    conn.rollback(); // Rollback if key retrieval fails
                    return -1;
                }
            } else {
                 LOGGER.log(Level.WARNING, "Tenant insertion failed (0 affected rows). Rolling back.");
                 conn.rollback(); // Rollback if insertion fails
                 return -1;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException during addTenant transaction for: " + tenant.getFirstName() + " " + tenant.getLastName() + ". Rolling back.", e);
            try {
                if (conn != null) {
                    conn.rollback();
                    LOGGER.log(Level.INFO, "Transaction rolled back due to SQLException.");
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Critical Error: Failed to rollback transaction after error.", ex);
            }
            return -1;
        } finally {
            if (generatedKeys != null) {
                try { generatedKeys.close(); } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error closing generated keys ResultSet", e); }
            }
            if (pstmt != null) {
                try { pstmt.close(); } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error closing prepared statement", e); }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close(); // Close the connection
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error resetting auto-commit or closing connection", e);
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
            pstmt.setString(3, tenant.getGuardianName());
            pstmt.setString(4, tenant.getEmergencyContactNumber());
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

    @Override
    public boolean assignTenantToRoom(int tenantId, Integer roomId) {
        Optional<Tenant> currentTenantOpt = findById(tenantId); // Use findById which should get its own connection
        if (!currentTenantOpt.isPresent()) {
            LOGGER.log(Level.WARNING, "Cannot assign room: Tenant not found with ID: " + tenantId);
            return false;
        }

        Integer oldRoomId = currentTenantOpt.get().getRoomId();
        // Prevent unnecessary updates if room isn't changing
        if (Objects.equals(oldRoomId, roomId)) {
             LOGGER.log(Level.INFO, "Tenant {0} is already assigned to room {1}. No update needed.", new Object[]{tenantId, roomId});
             return true; // Or false if you consider this a non-action
        }

        Connection conn = null;
        PreparedStatement pstmtUpdateTenant = null;
        boolean shouldCommit = false;
        RoomDAOImpl roomDAO; // Instantiate DAO once
        roomDAO = new RoomDAOImpl();

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Update tenant's room assignment
            pstmtUpdateTenant = conn.prepareStatement(ASSIGN_ROOM_SQL);
            pstmtUpdateTenant.setObject(1, roomId); // Can be null to unassign
            pstmtUpdateTenant.setInt(2, tenantId);

            int affectedRows = pstmtUpdateTenant.executeUpdate();
            if (affectedRows <= 0) {
                LOGGER.log(Level.WARNING, "(Tx) Failed to update tenant's room assignment for tenant ID: " + tenantId + ". Rolling back.");
                throw new SQLException("Failed to update tenant record.");
            }
            LOGGER.log(Level.INFO, "(Tx) Updated tenant {0} record to room {1}.", new Object[]{tenantId, roomId});

            // 2. Handle slots_available updates using the SAME transaction

            // If tenant was previously assigned to a room, increment that room's slots_available
            if (oldRoomId != null && oldRoomId > 0) {
                if (!roomDAO.incrementSlotsAvailable(oldRoomId, conn)) {
                    LOGGER.log(Level.WARNING, "(Tx) Failed to increment slots_available for previous room: " + oldRoomId + ". Rolling back.");
                    throw new SQLException("Failed to increment previous room slots.");
                }
                 LOGGER.log(Level.INFO, "(Tx) Incremented slots for old room {0}.", oldRoomId);
            }

            // If tenant is being assigned to a new room, decrement that room's slots_available
            if (roomId != null && roomId > 0) {
                if (!roomDAO.decrementSlotsAvailable(roomId, conn)) {
                    LOGGER.log(Level.WARNING, "(Tx) Failed to decrement slots_available for new room: " + roomId + ". Rolling back.");
                    throw new SQLException("Failed to decrement new room slots.");
                }
                LOGGER.log(Level.INFO, "(Tx) Decremented slots for new room {0}.", roomId);
            }

            shouldCommit = true; // Mark for commit
            conn.commit(); // Commit transaction
            LOGGER.log(Level.INFO, "Successfully assigned tenant {0} to room {1}. Transaction committed.", new Object[]{tenantId, roomId});
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException during assignTenantToRoom transaction for tenant " + tenantId + " to room " + roomId + ". Rolling back.", e);
            try {
                if (conn != null) {
                    conn.rollback();
                    LOGGER.log(Level.INFO, "Transaction rolled back due to SQLException.");
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Critical Error: Failed to rollback transaction after error.", ex);
            }
            return false;
        } finally {
            // Close resources
            if (pstmtUpdateTenant != null) {
                try { pstmtUpdateTenant.close(); } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error closing tenant update statement", e); }
            }
            // Close connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error resetting auto-commit or closing connection", e);
                }
            }
        }
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

        tenant.setGuardianName(rs.getString("guardian_name"));
        tenant.setEmergencyContactNumber(rs.getString("emergency_contact_number"));

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

    // --- Helper methods for specific assignments ---

    /**
     * Generic helper method to update a foreign key column for a tenant.
     * Uses its own connection and handles it fully (non-transactional).
     *
     * @param sql The SQL UPDATE statement (e.g., ASSIGN_GUARDIAN_SQL).
     * @param tenantId The ID of the tenant to update.
     * @param foreignKeyId The ID of the related entity (guardian, contact, user), or null to clear the key.
     * @return true if the update was successful, false otherwise.
     */
    private boolean updateTenantForeignKey(String sql, int tenantId, Integer foreignKeyId) {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set the foreign key ID (can be null)
            pstmt.setObject(1, foreignKeyId);
            // Set the tenant ID for the WHERE clause
            pstmt.setInt(2, tenantId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating foreign key for tenant: " + tenantId + " using SQL snippet: " + sql.substring(0, Math.min(sql.length(), 50)) + "...", e);
            return false;
        }
    }
}
