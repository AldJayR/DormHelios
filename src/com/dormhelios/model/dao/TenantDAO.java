package com.dormhelios.model.dao;

import com.dormhelios.model.entity.Tenant;
import com.dormhelios.model.entity.TenantWithRoom;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TenantDAO {

    Optional<Tenant> findById(int tenantId);

    List<Tenant> findAll();

    // New method to find all tenants with their room numbers
    List<TenantWithRoom> findAllWithRoomNumbers();

    List<Tenant> findByRoomId(int roomId); // Find tenants in a specific room

    List<Tenant> findByLastName(String lastName); // Example search

    int addTenant(Tenant tenant); // Return generated ID

    boolean updateTenant(Tenant tenant);

    // boolean deleteTenant(int tenantId); // Deprecated: Use setActiveStatus for soft delete

    int countAll(); // Counts only active tenants by default now
    
    /**
     * Count new tenants added within the specified date range
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return number of tenants added within the date range
     */
    int countNewTenantsByDateRange(LocalDate startDate, LocalDate endDate);

    boolean setActiveStatus(int tenantId, boolean isActive); // New method for soft delete/reactivate

    List<Tenant> findAllIncludingInactive(); // Optional: If needed to view inactive tenants

    boolean assignTenantToRoom(int tenantId, Integer roomId); // Helper for assignment

    boolean assignGuardianToTenant(int tenantId, Integer guardianId);

    boolean assignEmergencyContactToTenant(int tenantId, Integer contactId);

    boolean assignUserAccountToTenant(int tenantId, Integer userId);
}
