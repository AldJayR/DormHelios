package com.dormhelios.model.dao;

import com.dormhelios.model.entity.Guardian;
import java.util.List;
import java.util.Optional;

public interface GuardianDAO {

    Optional<Guardian> findById(int guardianId);

    Optional<Guardian> findByPhone(String phoneNumber); // Assuming phone is unique enough

    Optional<Guardian> findByEmail(String email);     // Assuming email is unique enough

    List<Guardian> findAll();

    int addGuardian(Guardian guardian); // Return generated ID

    boolean updateGuardian(Guardian guardian);

    // boolean deleteGuardian(int guardianId); // Deprecated: Use setActiveStatus for soft delete
    boolean setActiveStatus(int guardianId, boolean isActive); // New method for soft delete/reactivate
    
    public int countAll();

    List<Guardian> findAllIncludingInactive(); // Optional: to list inactive guardians if needed
}
