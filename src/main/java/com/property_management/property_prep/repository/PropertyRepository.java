package com.property_management.property_prep.repository;

import com.property_management.property_prep.entity.Property;
import com.property_management.property_prep.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByManager(User manager);
    List<Property> findByIsOccupiedFalse();

    // 🔴 Remove the old N+1 method:
    // int countByManager(User manager);

    // ✅ NEW: Single query to get counts for all managers (Optimization)
    @Query("SELECT p.manager, COUNT(p) FROM Property p GROUP BY p.manager")
    List<Object[]> countPropertiesByManager();
}
