package com.dormhelios.model.dao;

import com.dormhelios.model.entity.EmergencyContact;
import java.util.List;
import java.util.Optional;

public interface EmergencyContactDAO {

    Optional<EmergencyContact> findById(int contactId);

    Optional<EmergencyContact> findByPhone(String phoneNumber); // Assuming phone is unique enough

    List<EmergencyContact> findAll();

    int addContact(EmergencyContact contact); // Return generated ID

    boolean updateContact(EmergencyContact contact);

    boolean deleteContact(int contactId);
}
