package com.dormhelios.model.dao;

import com.dormhelios.model.entity.Room;
import com.dormhelios.util.DatabaseConnection; 

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RoomDAOImpl implements RoomDAO {

    private static final Logger LOGGER = Logger.getLogger(RoomDAOImpl.class.getName());

    // --- SQL Constants ---
    private static final String FIND_BY_ID_SQL = "SELECT * FROM ROOMS WHERE id = ?";
    private static final String FIND_BY_ROOM_NUMBER_SQL = "SELECT * FROM ROOMS WHERE room_number = ?";
    private static final String FIND_ALL_SQL = "SELECT * FROM ROOMS ORDER BY room_number";
    private static final String FIND_BY_STATUS_SQL = "SELECT * FROM ROOMS WHERE status = ? ORDER BY room_number";
    private static final String ADD_SQL = "INSERT INTO ROOMS (room_number, capacity, monthly_rate, status, description, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
    private static final String UPDATE_SQL = "UPDATE ROOMS SET room_number = ?, capacity = ?, monthly_rate = ?, status = ?, description = ?, updated_at = NOW() WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM ROOMS WHERE id = ?";
    private static final String COUNT_ALL_SQL = "SELECT COUNT(*) FROM ROOMS"; // New SQL
    private static final String COUNT_BY_STATUS_SQL = "SELECT COUNT(*) FROM ROOMS WHERE status = ?"; // New SQL

    @Override
    public Optional<Room> findById(int roomId) {
        // Use try-with-resources for automatic closing
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(FIND_BY_ID_SQL)) {

            pstmt.setInt(1, roomId); // Set parameter

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Map row to object using helper method
                    return Optional.of(mapResultSetToRoom(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding room by ID: " + roomId, e);
        }
        return Optional.empty(); // Return empty if not found or error
    }

    @Override
    public Optional<Room> findByRoomNumber(String roomNumber) {
        // Use try-with-resources
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(FIND_BY_ROOM_NUMBER_SQL)) {

            pstmt.setString(1, roomNumber); // Set parameter

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRoom(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding room by room number: " + roomNumber, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Room> findAll() {
        List<Room> rooms = new ArrayList<>();
        // Use try-with-resources
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(FIND_ALL_SQL); ResultSet rs = pstmt.executeQuery()) { // Execute query

            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs)); // Add mapped object
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all rooms", e);
        }
        return rooms; // Return list
    }

    @Override
    public List<Room> findByStatus(Room.RoomStatus status) {
        List<Room> rooms = new ArrayList<>();
        // Use try-with-resources
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(FIND_BY_STATUS_SQL)) {

            pstmt.setString(1, status.name()); // Set parameter using Enum name

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapResultSetToRoom(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding rooms by status: " + status, e);
        }
        return rooms;
    }

    @Override
    public int addRoom(Room room) {
        ResultSet generatedKeys = null;
        // Use try-with-resources, requesting generated keys
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(ADD_SQL, Statement.RETURN_GENERATED_KEYS)) {

            // Set parameters from the Room object
            pstmt.setString(1, room.getRoomNumber());
            pstmt.setInt(2, room.getCapacity());
            pstmt.setBigDecimal(3, room.getMonthlyRate());
            pstmt.setString(4, room.getStatus().name()); // Store Enum name
            pstmt.setString(5, room.getDescription());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Retrieve the auto-generated key
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Return the new room_id
                } else {
                    LOGGER.log(Level.WARNING, "Failed to retrieve generated key for new room.");
                    return -1;
                }
            } else {
                return -1; // Indicate insertion failure
            }
        } catch (SQLException e) {
            // Handle potential unique constraint violation for room_number gracefully
            if (e.getSQLState().startsWith("23")) { // Integrity constraint violation
                LOGGER.log(Level.WARNING, "Error adding room - possible duplicate room number: " + room.getRoomNumber(), e);
            } else {
                LOGGER.log(Level.SEVERE, "Error adding room: " + room.getRoomNumber(), e);
            }
            return -1; // Indicate failure
        } finally {
            // Ensure generatedKeys ResultSet is closed
            if (generatedKeys != null) {
                try {
                    generatedKeys.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error closing generated keys ResultSet", e);
                }
            }
        }
    }

    @Override
    public boolean updateRoom(Room room) {
        if (room.getRoomId() <= 0) {
            LOGGER.log(Level.WARNING, "Attempted to update room with invalid ID: 0");
            return false; // Cannot update without a valid ID
        }
        // Use try-with-resources
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(UPDATE_SQL)) {

            // Set parameters for the update
            pstmt.setString(1, room.getRoomNumber());
            pstmt.setInt(2, room.getCapacity());
            pstmt.setBigDecimal(3, room.getMonthlyRate());
            pstmt.setString(4, room.getStatus().name());
            pstmt.setString(5, room.getDescription());
            // Set the ID for the WHERE clause
            pstmt.setInt(6, room.getRoomId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0; // Return true if updated

        } catch (SQLException e) {
            // Handle potential unique constraint violation for room_number gracefully
            if (e.getSQLState().startsWith("23")) {
                LOGGER.log(Level.WARNING, "Error updating room - possible duplicate room number: " + room.getRoomNumber(), e);
            } else {
                LOGGER.log(Level.SEVERE, "Error updating room: " + room.getRoomId(), e);
            }
            return false;
        }
    }

    @Override
    public boolean deleteRoom(int roomId) {
        // CRITICAL: Check if tenants are assigned to this room before deleting.
        // The database foreign key constraint might prevent deletion if tenants are linked.
        // Consider adding application logic here to check TenantDAO.findByRoomId(roomId)
        // and prevent deletion if the list is not empty, or require tenants to be moved first.
        LOGGER.log(Level.WARNING, "Attempting to delete room with ID: " + roomId + ". Ensure no tenants are assigned.");
        // Use try-with-resources
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(DELETE_SQL)) {

            pstmt.setInt(1, roomId); // Set parameter
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                LOGGER.log(Level.WARNING, "Room with ID " + roomId + " not found for deletion.");
            }
            return affectedRows > 0; // Return true if deleted

        } catch (SQLException e) {
            // Handle foreign key constraint violation specifically
            if (e.getSQLState().startsWith("23")) { // Integrity constraint violation
                LOGGER.log(Level.SEVERE, "Error deleting room ID " + roomId + ". Failed due to foreign key constraint (likely tenants assigned).", e);
            } else {
                LOGGER.log(Level.SEVERE, "Error deleting room: " + roomId, e);
            }
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
            LOGGER.log(Level.SEVERE, "Error counting all rooms", e);
        }
        return 0;
    }

    @Override
    public int countByStatus(Room.RoomStatus status) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(COUNT_BY_STATUS_SQL)) {
            pstmt.setString(1, status.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting rooms by status: " + status, e);
        }
        return 0;
    }
    
    @Override
    public boolean setActiveStatus(int roomId, boolean status)
    {
        return status;
    }

    // --- Helper Method for Mapping ---
    /**
     * Maps a row from the ResultSet to a Room object.
     *
     * @param rs The ResultSet positioned at the current row.
     * @return The mapped Room object.
     * @throws SQLException if a database access error occurs.
     */
    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setRoomId(rs.getInt("id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setCapacity(rs.getInt("capacity"));
        room.setMonthlyRate(rs.getBigDecimal("monthly_rate"));

        // Handle Enum for status, providing a default if the DB value is invalid
        try {
            room.setStatus(Room.RoomStatus.valueOf(rs.getString("status").toUpperCase()));
        } catch (IllegalArgumentException | NullPointerException e) {
            LOGGER.log(Level.WARNING, "Invalid or NULL room status found in DB for room ID: " + room.getRoomId() + ". Defaulting to VACANT.");
            room.setStatus(Room.RoomStatus.VACANT); // Default fallback
        }

        room.setDescription(rs.getString("description")); // Handle potential null

        // Handle Timestamps
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        if (createdAtTs != null) {
            room.setCreatedAt(createdAtTs.toLocalDateTime());
        }
        Timestamp updatedAtTs = rs.getTimestamp("updated_at");
        if (updatedAtTs != null) {
            room.setUpdatedAt(updatedAtTs.toLocalDateTime());
        }

        return room;
    }
}
