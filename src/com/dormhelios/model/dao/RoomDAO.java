package com.dormhelios.model.dao;

import com.dormhelios.model.entity.Room;
import java.util.List;
import java.util.Optional;

public interface RoomDAO {

    Optional<Room> findById(int roomId);

    Optional<Room> findByRoomNumber(String roomNumber);

    List<Room> findAll();

    List<Room> findByStatus(Room.RoomStatus status); // Find vacant/occupied rooms

    int countAll(); // New method
    int countByStatus(Room.RoomStatus status); // New method

    int addRoom(Room room); // Return generated ID
    
    boolean setActiveStatus(int roomId, boolean status);

    boolean updateRoom(Room room);

    boolean deleteRoom(int roomId); // Careful! Only if no tenants assigned?
}
