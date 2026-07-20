package com.property_management.property_prep.repository;

import com.property_management.property_prep.entity.Property;
import com.property_management.property_prep.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByManager(User manager);
    List<Property> findByIsOccupiedFalse();
    int countByManager(User manager);  // <-- NEW
}