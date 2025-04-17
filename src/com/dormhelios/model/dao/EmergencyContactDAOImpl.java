package com.dormhelios.model.dao;

import com.dormhelios.model.entity.EmergencyContact;
import com.dormhelios.util.DatabaseConnection; // Assumes this utility provides connections

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC Implementation of the EmergencyContactDAO interface.
 */
public class EmergencyContactDAOImpl implements EmergencyContactDAO {

    private static final Logger LOGGER = Logger.getLogger(EmergencyContactDAOImpl.class.getName());

    // --- SQL Constants ---
    private static final String FIND_BY_ID_SQL = "SELECT * FROM EMERGENCY_CONTACTS WHERE contact_id = ?";
    private static final String FIND_BY_PHONE_SQL = "SELECT * FROM EMERGENCY_CONTACTS WHERE phone_number = ?";
    private static final String FIND_ALL_SQL = "SELECT * FROM EMERGENCY_CONTACTS ORDER BY name";
    private static final String ADD_SQL = "INSERT INTO EMERGENCY_CONTACTS (name, phone_number, relationship, created_at, updated_at) VALUES (?, ?, ?, NOW(), NOW())";
    private static final String UPDATE_SQL = "UPDATE EMERGENCY_CONTACTS SET name = ?, phone_number = ?, relationship = ?, updated_at = NOW() WHERE contact_id = ?";
    private static final String DELETE_SQL = "DELETE FROM EMERGENCY_CONTACTS WHERE contact_id = ?";

    @Override
    public Optional<EmergencyContact> findById(int contactId) {
        // Use try-with-resources for automatic closing
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(FIND_BY_ID_SQL)) {

            pstmt.setInt(1, contactId); // Set parameter

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Map row to object using helper method
                    return Optional.of(mapResultSetToEmergencyContact(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding emergency contact by ID: " + contactId, e);
        }
        return Optional.empty(); // Return empty if not found or error
    }

    @Override
    public Optional<EmergencyContact> findByPhone(String phoneNumber) {
        // Use try-with-resources
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(FIND_BY_PHONE_SQL)) {

            pstmt.setString(1, phoneNumber); // Set parameter

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEmergencyContact(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding emergency contact by phone number: " + phoneNumber, e);
        }
        return Optional.empty();
    }

    @Override
    public List<EmergencyContact> findAll() {
        List<EmergencyContact> contacts = new ArrayList<>();
        // Use try-with-resources
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = pstmt.executeQuery()) { // Execute query

            while (rs.next()) {
                contacts.add(mapResultSetToEmergencyContact(rs)); // Add mapped object
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all emergency contacts", e);
        }
        return contacts; // Return list
    }

    @Override
    public int addContact(EmergencyContact contact) {
        ResultSet generatedKeys = null;
        // Use try-with-resources, requesting generated keys
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(ADD_SQL, Statement.RETURN_GENERATED_KEYS)) {

            // Set parameters from the EmergencyContact object
            pstmt.setString(1, contact.getName());
            pstmt.setString(2, contact.getPhoneNumber());
            pstmt.setString(3, contact.getRelationship());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Retrieve the auto-generated key
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Return the new contact_id
                } else {
                    LOGGER.log(Level.WARNING, "Failed to retrieve generated key for new emergency contact.");
                    return -1;
                }
            } else {
                return -1; // Indicate insertion failure
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding emergency contact: " + contact.getName(), e);
            return -1; // Indicate failure
        } finally {
            // Ensure generatedKeys ResultSet is closed
             if (generatedKeys != null) {
                 try { generatedKeys.close(); } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error closing generated keys ResultSet", e); }
             }
        }
    }

    @Override
    public boolean updateContact(EmergencyContact contact) {
        if (contact.getContactId() <= 0) {
             LOGGER.log(Level.WARNING, "Attempted to update emergency contact with invalid ID: 0");
            return false; // Cannot update without a valid ID
        }
        // Use try-with-resources
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_SQL)) {

            // Set parameters for the update
            pstmt.setString(1, contact.getName());
            pstmt.setString(2, contact.getPhoneNumber());
            pstmt.setString(3, contact.getRelationship());
            // Set the ID for the WHERE clause
            pstmt.setInt(4, contact.getContactId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0; // Return true if updated

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating emergency contact: " + contact.getContactId(), e);
            return false;
        }
    }

    @Override
    public boolean deleteContact(int contactId) {
        // Consider implications: Is this contact linked to any tenants?
        // Database foreign key constraints might prevent deletion if linked.
        // A soft delete might be preferable in some scenarios.
        // Use try-with-resources
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_SQL)) {

            pstmt.setInt(1, contactId); // Set parameter
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0; // Return true if deleted

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting emergency contact: " + contactId, e);
            return false;
        }
    }

    // --- Helper Method for Mapping ---

    /**
     * Maps a row from the ResultSet to an EmergencyContact object.
     *
     * @param rs The ResultSet positioned at the current row.
     * @return The mapped EmergencyContact object.
     * @throws SQLException if a database access error occurs.
     */
    private EmergencyContact mapResultSetToEmergencyContact(ResultSet rs) throws SQLException {
        EmergencyContact contact = new EmergencyContact();
        contact.setContactId(rs.getInt("contact_id"));
        contact.setName(rs.getString("name"));
        contact.setPhoneNumber(rs.getString("phone_number"));
        contact.setRelationship(rs.getString("relationship")); // Handle potential null

        // Handle Timestamps
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        if (createdAtTs != null) {
            contact.setCreatedAt(createdAtTs.toLocalDateTime());
        }
        Timestamp updatedAtTs = rs.getTimestamp("updated_at");
        if (updatedAtTs != null) {
            contact.setUpdatedAt(updatedAtTs.toLocalDateTime());
        }

        return contact;
    }
}