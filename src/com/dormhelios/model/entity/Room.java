package com.dormhelios.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a physical room within the dormitory.
 */
public class Room {

    public enum RoomStatus {
        VACANT, OCCUPIED, UNDER_MAINTENANCE
    }

    private int roomId;
    private String roomNumber; // Unique identifier like "A101"
    private int capacity;
    private BigDecimal monthlyRate;
    private RoomStatus status;
    private String description; // Nullable
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive; // Added for soft delete

    public Room() {
        // Set defaults
        this.capacity = 1;
        this.status = RoomStatus.VACANT;
        this.monthlyRate = BigDecimal.ZERO;
        this.isActive = true; // Default new rooms to active
    }

    public Room(String roomNumber, int capacity, BigDecimal monthlyRate) {
        this(); // Call default constructor
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.monthlyRate = monthlyRate;
    }

    // --- Getters and Setters ---

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public BigDecimal getMonthlyRate() {
        return monthlyRate;
    }

    public void setMonthlyRate(BigDecimal monthlyRate) {
        this.monthlyRate = Objects.requireNonNullElse(monthlyRate, BigDecimal.ZERO);
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = Objects.requireNonNullElse(status, RoomStatus.VACANT);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // --- equals, hashCode, toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        if (roomId != 0) {
            return roomId == room.roomId;
        }
        // Fallback to unique room number
        return Objects.equals(roomNumber, room.roomNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId != 0 ? roomId : roomNumber);
    }

    @Override
    public String toString() {
        return "Room{" +
               "roomId=" + roomId +
               ", roomNumber='" + roomNumber + '\'' +
               ", status=" + status +
               ", monthlyRate=" + monthlyRate +
               '}';
    }
}