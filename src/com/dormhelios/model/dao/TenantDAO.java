package com.dormhelios.model.dao;

import com.dormhelios.model.entity.Tenant;
import java.util.List;
import java.util.Optional;

public interface TenantDAO {

    Optional<Tenant> findById(int tenantId);

    List<Tenant> findAll();

    List<Tenant> findByRoomId(int roomId); // Find tenants in a specific room

    List<Tenant> findByLastName(String lastName); // Example search

    int addTenant(Tenant tenant); // Return generated ID

    boolean updateTenant(Tenant tenant);

    // boolean deleteTenant(int tenantId); // Deprecated: Use setActiveStatus for soft delete

    int countAll(); // Counts only active tenants by default now

    boolean setActiveStatus(int tenantId, boolean isActive); // New method for soft delete/reactivate

    List<Tenant> findAllIncludingInactive(); // Optional: If needed to view inactive tenants

    boolean assignTenantToRoom(int tenantId, Integer roomId); // Helper for assignment

    boolean assignGuardianToTenant(int tenantId, Integer guardianId);

    boolean assignEmergencyContactToTenant(int tenantId, Integer contactId);

    boolean assignUserAccountToTenant(int tenantId, Integer userId);
}
