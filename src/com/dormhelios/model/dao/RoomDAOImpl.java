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
    private static final String FIND_ALL_SQL = "SELECT * FROM ROOMS WHERE is_active = 1 ORDER BY room_number";
    private static final String FIND_BY_STATUS_SQL = "SELECT * FROM ROOMS WHERE status = ? AND is_active = 1 ORDER BY room_number";
    private static final String ADD_SQL = "INSERT INTO ROOMS (room_number, capacity, slots_available, monthly_rate, status, description, created_at, updated_at, is_active) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW(), 1)";
    private static final String UPDATE_SQL = "UPDATE ROOMS SET room_number = ?, capacity = ?, slots_available = ?, monthly_rate = ?, status = ?, description = ?, updated_at = NOW() WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM ROOMS WHERE id = ?";
    private static final String COUNT_ALL_SQL = "SELECT COUNT(*) FROM ROOMS WHERE is_active = 1"; // Updated SQL
    private static final String COUNT_BY_STATUS_SQL = "SELECT COUNT(*) FROM ROOMS WHERE status = ? AND is_active = 1"; // Updated SQL
    private static final String UPDATE_SLOTS_AVAILABLE_SQL = "UPDATE ROOMS SET slots_available = ?, updated_at = NOW() WHERE id = ?";
    private static final String DECREMENT_SLOTS_AVAILABLE_SQL = "UPDATE ROOMS SET slots_available = slots_available - 1, updated_at = NOW() WHERE id = ? AND slots_available > 0";
    private static final String INCREMENT_SLOTS_AVAILABLE_SQL = "UPDATE ROOMS SET slots_available = slots_available + 1, updated_at = NOW() WHERE id = ? AND slots_available < capacity";

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
            pstmt.setInt(3, room.getSlotsAvailable());
            pstmt.setBigDecimal(4, room.getMonthlyRate());
            pstmt.setString(5, room.getStatus().name()); // Store Enum name
            pstmt.setString(6, room.getDescription());

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
            pstmt.setInt(3, room.getSlotsAvailable());
            pstmt.setBigDecimal(4, room.getMonthlyRate());
            pstmt.setString(5, room.getStatus().name());
            pstmt.setString(6, room.getDescription());
            // Set the ID for the WHERE clause
            pstmt.setInt(7, room.getRoomId());

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
        // SQL for updating just the is_active column
        String updateStatusSQL = "UPDATE ROOMS SET is_active = ?, updated_at = NOW() WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(updateStatusSQL)) {
            
            // Set parameters - convert boolean to 1 or 0
            pstmt.setInt(1, status ? 1 : 0);
            pstmt.setInt(2, roomId);
            
            // Execute the update
            int affectedRows = pstmt.executeUpdate();
            
            // Return true if the update was successful
            return affectedRows > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating room active status for room ID: " + roomId, e);
            return false;
        }
    }
    
    @Override
    public boolean decrementSlotsAvailable(int roomId, Connection conn) throws SQLException { // Add conn parameter, throws SQLException
        final String sql = "UPDATE ROOMS SET slots_available = slots_available - 1, updated_at = NOW() " +
                          "WHERE id = ? AND slots_available > 0";

        // Use the provided connection, DO NOT get/close connection here
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                // Log, but let the caller decide rollback based on return value/exception
                LOGGER.log(Level.WARNING, "(Tx) Failed to decrement slots_available for roomId: " + roomId +
                                         ". Room may not exist or have no available slots.");
                return false;
            }

            return true;
        } catch (SQLException e) {
            // Log the error but re-throw it so the transaction can be rolled back
            LOGGER.log(Level.SEVERE, "(Tx) Error decrementing slots_available for room: " + roomId, e);
            throw e; // Re-throw SQLException
        }
    }

    @Override
    public boolean incrementSlotsAvailable(int roomId, Connection conn) throws SQLException { // Add conn parameter, throws SQLException
        final String sql = "UPDATE ROOMS SET slots_available = slots_available + 1, updated_at = NOW() " +
                          "WHERE id = ? AND slots_available < capacity";

        // Use the provided connection, DO NOT get/close connection here
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                LOGGER.log(Level.WARNING, "(Tx) Failed to increment slots_available for roomId: " + roomId +
                                         ". Room may not exist or is already at full capacity.");
                return false;
            }

            return true;
        } catch (SQLException e) {
            // Log the error but re-throw it so the transaction can be rolled back
            LOGGER.log(Level.SEVERE, "(Tx) Error incrementing slots_available for room: " + roomId, e);
            throw e; // Re-throw SQLException
        }
    }

    /**
     * Decrements slots available for a room using its own connection (non-transactional).
     * @param roomId The ID of the room.
     * @return true if successful, false otherwise.
     */
    public boolean decrementSlotsAvailable(int roomId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
             // We can potentially call the transactional version here,
             // but managing commit/rollback for a single operation is simple.
             conn.setAutoCommit(true); // Ensure auto-commit is on for single operation
             // Need to handle the SQLException thrown by the transactional version
             try {
                 return decrementSlotsAvailable(roomId, conn);
             } catch (SQLException innerEx) {
                 LOGGER.log(Level.SEVERE, "SQLException occurred within non-tx decrementSlotsAvailable wrapper for room: " + roomId, innerEx);
                 return false;
             }
        } catch (SQLException e) {
             LOGGER.log(Level.SEVERE, "Error getting connection for non-tx decrementSlotsAvailable for room: " + roomId, e);
             return false;
        }
    }

    /**
     * Increments slots available for a room using its own connection (non-transactional).
     * @param roomId The ID of the room.
     * @return true if successful, false otherwise.
     */
    public boolean incrementSlotsAvailable(int roomId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(true); // Ensure auto-commit is on for single operation
            // Need to handle the SQLException thrown by the transactional version
            try {
                return incrementSlotsAvailable(roomId, conn);
            } catch (SQLException innerEx) {
                LOGGER.log(Level.SEVERE, "SQLException occurred within non-tx incrementSlotsAvailable wrapper for room: " + roomId, innerEx);
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting connection for non-tx incrementSlotsAvailable for room: " + roomId, e);
            return false;
        }
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
        room.setSlotsAvailable(rs.getInt("slots_available"));
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
