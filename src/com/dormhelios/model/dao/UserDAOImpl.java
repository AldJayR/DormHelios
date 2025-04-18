package com.dormhelios.model.dao;

import com.dormhelios.model.entity.User;
import com.dormhelios.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level; // Using java.util.logging for simplicity, consider SLF4j
import java.util.logging.Logger;

public class UserDAOImpl implements UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAOImpl.class.getName());

    // SQL Statements (Use constants)
    private static final String FIND_BY_USERNAME_SQL = "SELECT * FROM USERS WHERE username = ?";
    private static final String FIND_BY_EMAIL_SQL = "SELECT * FROM USERS WHERE email = ?";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM USERS WHERE id = ?";
    private static final String FIND_ALL_SQL = "SELECT * FROM USERS ORDER BY full_name";
    private static final String ADD_USER_SQL = "INSERT INTO USERS (username, password_hash, full_name, role, email, phone_number, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
    private static final String UPDATE_USER_SQL = "UPDATE USERS SET username = ?, password_hash = ?, full_name = ?, role = ?, email = ?, phone_number = ?, is_active = ?, updated_at = NOW() WHERE user_id = ?";
    private static final String REGISTER_USER_SQL = """
                                                    INSERT INTO USERS (email, password_hash, is_active, created_at, updated_at)
                                                    VALUES (?, ?, ?, NOW(), NOW())
                                                    """;
    private static final String DELETE_USER_SQL = "DELETE FROM USERS WHERE id = ?";
    // private static final String SET_ACTIVE_SQL = "UPDATE USERS SET is_active = ?, updated_at = NOW() WHERE user_id = ?"; // For soft delete

    @Override
    public Optional<User> findByUsername(String username) {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(FIND_BY_USERNAME_SQL)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by username: " + username, e);
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(FIND_BY_EMAIL_SQL)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by email: " + email, e);
        }
        return Optional.empty();
    }
    

    @Override
    public Optional<User> findById(int userId) {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(FIND_BY_ID_SQL)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by ID: " + userId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(FIND_ALL_SQL); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all users", e);
        }
        return users;
    }

    @Override
    public int addUser(User user) {
        ResultSet generatedKeys = null;
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(REGISTER_USER_SQL, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setBoolean(3, user.isActive());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Return the generated ID
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding user: " + user.getUsername(), e);
        } finally {
            // Ensure ResultSet is closed
            if (generatedKeys != null) {
                try {
                    generatedKeys.close();
                } catch (SQLException e) {
                    /* ignore */ }
            }
        }
        return -1; // Indicate failure
    }


    @Override
    public boolean updateUser(User user) {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(UPDATE_USER_SQL)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            // pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getRole().name());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getPhoneNumber());
            pstmt.setBoolean(7, user.isActive());
            pstmt.setInt(8, user.getUserId()); // WHERE clause

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user: " + user.getUserId(), e);
            return false;
        }
    }

    @Override
    public boolean deleteUser(int userId) {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(DELETE_USER_SQL)) {

            pstmt.setInt(1, userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting user: " + userId, e);
            return false;
            // Consider implications of foreign key constraints
        }
    }

    // Helper to map ResultSet to User object
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("id"));
       user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash")); // Important: Never expose this unnecessarily
        user.setFirstName(rs.getString("first_name"));
        user.setSurname(rs.getString("surname"));

       /*
        try {
            user.setRole(User.Role.valueOf(rs.getString("role").toUpperCase()));
        } catch (IllegalArgumentException | NullPointerException e) {
            LOGGER.log(Level.WARNING, "Invalid role found in database for user ID: " + user.getUserId(), e);
            // Handle appropriately - perhaps set a default role or log error prominently
            
        }
*/
        user.setRole(User.Role.TENANT); // Example fallback
        user.setEmail(rs.getString("email"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setActive(rs.getBoolean("is_active"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return user;
    }
}
