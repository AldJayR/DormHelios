package com.dormhelios.model.entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class EmergencyContact {

    private int contactId;
    private String name;
    private String phoneNumber; // Should be unique if used as a business key
    private String relationship; // e.g., "Father", "Friend", Nullable
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EmergencyContact() {
    }

    public EmergencyContact(String name, String phoneNumber, String relationship) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.relationship = relationship;
    }

    // --- Getters and Setters ---

    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
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

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
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

    // --- equals, hashCode, toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmergencyContact that = (EmergencyContact) o;
        if (contactId != 0) {
            return contactId == that.contactId;
        }
        // Fallback to unique phone number if ID is 0
        return Objects.equals(phoneNumber, that.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contactId != 0 ? contactId : phoneNumber);
    }

    @Override
    public String toString() {
        return "EmergencyContact{" +
               "contactId=" + contactId +
               ", name='" + name + '\'' +
               ", phoneNumber='" + phoneNumber + '\'' +
               '}';
    }
}