package com.dormhelios.model.dao;

import com.dormhelios.model.entity.Guardian;
import com.dormhelios.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GuardianDAOImpl implements GuardianDAO {

    private static final Logger LOGGER = Logger.getLogger(GuardianDAOImpl.class.getName());

    private static final String FIND_BY_ID_SQL = "SELECT * FROM GUARDIANS WHERE guardian_id = ?";
    private static final String FIND_BY_PHONE_SQL = "SELECT * FROM GUARDIANS WHERE phone_number = ?";
    private static final String FIND_BY_EMAIL_SQL = "SELECT * FROM GUARDIANS WHERE email = ?";
    private static final String FIND_ALL_SQL = "SELECT * FROM GUARDIANS ORDER BY name";
    private static final String ADD_SQL = "INSERT INTO GUARDIANS (name, phone_number, email, address, created_at, updated_at) VALUES (?, ?, ?, ?, NOW(), NOW())";
    private static final String UPDATE_SQL = "UPDATE GUARDIANS SET name = ?, phone_number = ?, email = ?, address = ?, updated_at = NOW() WHERE guardian_id = ?";
    private static final String DELETE_SQL = "DELETE FROM GUARDIANS WHERE guardian_id = ?";
    private static final String COUNT_ALL_SQL = "SELECT COUNT(*) FROM GUARDIANS WHERE is_active = TRUE";
    private static final String SET_ACTIVE_STATUS_SQL = "UPDATE GUARDIANS SET is_active = ?, updated_at = NOW() WHERE guardian_id = ?";

    @Override
    public Optional<Guardian> findById(int guardianId) {
        final String sql = FIND_BY_ID_SQL + " AND is_active = TRUE";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, guardianId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGuardian(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding guardian by ID: " + guardianId, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Guardian> findByPhone(String phoneNumber) {
        final String sql = FIND_BY_PHONE_SQL + " AND is_active = TRUE";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phoneNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGuardian(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding guardian by phone: " + phoneNumber, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Guardian> findByEmail(String email) {
        final String sql = FIND_BY_EMAIL_SQL + " AND is_active = TRUE";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGuardian(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding guardian by email: " + email, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Guardian> findAll() {
        List<Guardian> guardians = new ArrayList<>();
        final String sql = FIND_ALL_SQL.replace("ORDER BY", "WHERE is_active = TRUE ORDER BY");
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                guardians.add(mapResultSetToGuardian(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all guardians", e);
        }
        return guardians;
    }

    @Override
    public int addGuardian(Guardian guardian) {
        ResultSet generatedKeys = null;
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(ADD_SQL, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, guardian.getName());
            pstmt.setString(2, guardian.getPhoneNumber());
            pstmt.setString(3, guardian.getEmail());
            pstmt.setString(4, guardian.getAddress());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding guardian: " + guardian.getName(), e);
        } finally {
            if (generatedKeys != null) try {
                generatedKeys.close();
            } catch (SQLException e) {
                /* ignore */ }
        }
        return -1;
    }

    @Override
    public boolean updateGuardian(Guardian guardian) {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(UPDATE_SQL)) {
            pstmt.setString(1, guardian.getName());
            pstmt.setString(2, guardian.getPhoneNumber());
            pstmt.setString(3, guardian.getEmail());
            pstmt.setString(4, guardian.getAddress());
            pstmt.setInt(5, guardian.getGuardianId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating guardian: " + guardian.getGuardianId(), e);
            return false;
        }
    }

    /**
     * Deprecated hard delete. Use setActiveStatus instead.
     */
  

    @Override
    public List<Guardian> findAllIncludingInactive() {
        List<Guardian> guardians = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(FIND_ALL_SQL); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                guardians.add(mapResultSetToGuardian(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all guardians (including inactive)", e);
        }
        return guardians;
    }

    @Override
    public int countAll() {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(COUNT_ALL_SQL); ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting all active guardians", e);
        }
        return 0;
    }

    @Override
    public boolean setActiveStatus(int guardianId, boolean isActive) {
        LOGGER.log(Level.INFO, "Setting active status to {0} for guardian ID: {1}", new Object[]{isActive, guardianId});
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(SET_ACTIVE_STATUS_SQL)) {
            pstmt.setBoolean(1, isActive);
            pstmt.setInt(2, guardianId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error setting active status for guardian: " + guardianId, e);
            return false;
        }
    }

    // Helper
    private Guardian mapResultSetToGuardian(ResultSet rs) throws SQLException {
        Guardian g = new Guardian();
        g.setGuardianId(rs.getInt("id"));
        g.setName(rs.getString("name"));
        g.setPhoneNumber(rs.getString("phone_number"));
        g.setEmail(rs.getString("email"));
        g.setAddress(rs.getString("address"));
        g.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        g.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        g.setActive(rs.getBoolean("is_active"));
        return g;
    }
}
