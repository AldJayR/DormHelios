package com.dormhelios.model.entity;

import java.time.LocalDateTime;
import java.util.Objects;


public class Guardian {

    private int guardianId;
    private String name;
    private String phoneNumber; // Nullable, potentially unique
    private String email;       // Nullable, potentially unique
    private String address;     // Nullable
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive; // Added for soft delete

    public Guardian() {
        this.isActive = true; // Default new guardians to active
    }

    public Guardian(String name, String phoneNumber, String email, String address) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.isActive = true; // Default new guardians to active
    }

    // --- Getters and Setters ---

    public int getGuardianId() {
        return guardianId;
    }

    public void setGuardianId(int guardianId) {
        this.guardianId = guardianId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
        this.isActive = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Guardian guardian = (Guardian) o;
        if (guardianId != 0) {
            return guardianId == guardian.guardianId;
        }
        return Objects.equals(phoneNumber, guardian.phoneNumber) && Objects.equals(email, guardian.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guardianId != 0 ? guardianId : phoneNumber, email);
    }

    @Override
    public String toString() {
        return "Guardian{" +
               "guardianId=" + guardianId +
               ", name='" + name + '\'' +
               ", phoneNumber='" + phoneNumber + '\'' +
               '}';
    }
}