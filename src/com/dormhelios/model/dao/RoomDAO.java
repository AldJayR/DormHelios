package com.dormhelios.model.dao;

import com.dormhelios.model.entity.Room;
import java.util.List;
import java.util.Optional;
import java.sql.Connection;
import java.sql.SQLException;

public interface RoomDAO {

    // --- Basic CRUD operations ---
    Optional<Room> findById(int roomId);
    Optional<Room> findByRoomNumber(String roomNumber);
    List<Room> findAll();
    List<Room> findByStatus(Room.RoomStatus status);
    int addRoom(Room room);
    boolean updateRoom(Room room);
    boolean deleteRoom(int roomId);

    // --- Additional operations ---
    int countAll();
    int countByStatus(Room.RoomStatus status);
    boolean setActiveStatus(int roomId, boolean status);
    boolean decrementSlotsAvailable(int roomId, Connection conn) throws SQLException; // Add Connection parameter and throws SQLException
    boolean incrementSlotsAvailable(int roomId, Connection conn) throws SQLException; // Add Connection parameter and throws SQLException

    // --- Slots management operations ---
    /*
    boolean decrementSlotsAvailable(int roomId);
    boolean incrementSlotsAvailable(int roomId);
*/
}
