package com.dormhelios.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a student tenant residing in the dormitory.
 */
public class Tenant {

    public enum DepositStatus {
        PAID, PENDING, REFUNDED, PARTIAL_REFUND
    }

    private int tenantId;
    private Integer userId; // FK to Users (Nullable) - For tenant login
    private Integer roomId; // FK to Rooms (Nullable) - Current room assignment
    private String guardianName; // Name of guardian (Nullable)
    private String emergencyContactNumber; // Emergency contact phone number (Nullable)
    private String firstName;
    private String lastName;
    private String studentIdNumber; // Nullable
    private String email;
    private String phoneNumber;
    private String permanentAddress; // Nullable
    private LocalDate leaseStartDate; // Nullable
    private LocalDate leaseEndDate;   // Nullable
    private BigDecimal securityDepositAmount;
    private DepositStatus securityDepositStatus;
    private String notes; // Nullable
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive; // Added for soft delete

    public Tenant() {
        // Set defaults
        this.securityDepositAmount = BigDecimal.ZERO;
        this.securityDepositStatus = DepositStatus.PENDING;
        this.isActive = true; // Default new tenants to active
    }

    // --- Getters and Setters ---

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public String getEmergencyContactNumber() {
        return emergencyContactNumber;
    }

    public void setEmergencyContactNumber(String emergencyContactNumber) {
        this.emergencyContactNumber = emergencyContactNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getStudentIdNumber() {
        return studentIdNumber;
    }

    public void setStudentIdNumber(String studentIdNumber) {
        this.studentIdNumber = studentIdNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPermanentAddress() {
        return permanentAddress;
    }

    public void setPermanentAddress(String permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    public LocalDate getLeaseStartDate() {
        return leaseStartDate;
    }

    public void setLeaseStartDate(LocalDate leaseStartDate) {
        this.leaseStartDate = leaseStartDate;
    }

    public LocalDate getLeaseEndDate() {
        return leaseEndDate;
    }

    public void setLeaseEndDate(LocalDate leaseEndDate) {
        this.leaseEndDate = leaseEndDate;
    }

    public BigDecimal getSecurityDepositAmount() {
        return securityDepositAmount;
    }

    public void setSecurityDepositAmount(BigDecimal securityDepositAmount) {
        // Ensure non-null, default to zero if needed
        this.securityDepositAmount = Objects.requireNonNullElse(securityDepositAmount, BigDecimal.ZERO);
    }

    public DepositStatus getSecurityDepositStatus() {
        return securityDepositStatus;
    }

    public void setSecurityDepositStatus(DepositStatus securityDepositStatus) {
        this.securityDepositStatus = Objects.requireNonNullElse(securityDepositStatus, DepositStatus.PENDING);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
        Tenant tenant = (Tenant) o;
        // Base equality on the primary key if set
        if (tenantId != 0) {
            return tenantId == tenant.tenantId;
        }
        // Fallback if ID is not set (less reliable for non-unique tenants before saving)
        return Objects.equals(firstName, tenant.firstName) &&
               Objects.equals(lastName, tenant.lastName) &&
               Objects.equals(email, tenant.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId != 0 ? tenantId : Objects.hash(firstName, lastName, email));
    }

    @Override
    public String toString() {
        return "Tenant{" +
               "tenantId=" + tenantId +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", email='" + email + '\'' +
               ", roomId=" + roomId +
               '}';
    }
}