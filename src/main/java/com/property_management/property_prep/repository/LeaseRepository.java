package com.property_management.property_prep.repository;

import com.property_management.property_prep.entity.LeaseAgreement;
import com.property_management.property_prep.entity.Property;
import com.property_management.property_prep.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeaseRepository extends JpaRepository<LeaseAgreement, Long> {
    List<LeaseAgreement> findByProperty(Property property);
    List<LeaseAgreement> findByTenant(User tenant);
    List<LeaseAgreement> findByPropertyIn(List<Property> properties); // <-- ADD THIS
}