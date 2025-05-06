package com.dormhelios.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data class that extends Tenant with additional room number information.
 * Used for displaying tenants with their room numbers in the UI.
 */
public class TenantWithRoom extends Tenant {
    private String roomNumber;
    
    public TenantWithRoom(Tenant tenant, String roomNumber) {
        // Copy all fields from the tenant
        this.setTenantId(tenant.getTenantId());
        this.setFirstName(tenant.getFirstName());
        this.setLastName(tenant.getLastName());
        this.setStudentIdNumber(tenant.getStudentIdNumber());
        this.setEmail(tenant.getEmail());
        this.setPhoneNumber(tenant.getPhoneNumber());
        this.setPermanentAddress(tenant.getPermanentAddress());
        this.setLeaseStartDate(tenant.getLeaseStartDate());
        this.setLeaseEndDate(tenant.getLeaseEndDate());
        this.setSecurityDepositAmount(tenant.getSecurityDepositAmount());
        this.setSecurityDepositStatus(tenant.getSecurityDepositStatus());
        this.setNotes(tenant.getNotes());
        this.setCreatedAt(tenant.getCreatedAt());
        this.setUpdatedAt(tenant.getUpdatedAt());
        this.setActive(tenant.isActive());
        this.setRoomId(tenant.getRoomId());
        this.setGuardianName(tenant.getGuardianName());
        this.setEmergencyContactNumber(tenant.getEmergencyContactNumber());
        this.setUserId(tenant.getUserId());
        
        // Set the additional room number field
        this.roomNumber = roomNumber;
    }
    
    public String getRoomNumber() {
        return roomNumber;
    }
    
    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
}