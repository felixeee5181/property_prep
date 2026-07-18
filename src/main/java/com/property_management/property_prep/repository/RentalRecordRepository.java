package com.property_management.property_prep.repository;

import com.property_management.property_prep.entity.RentalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRecordRepository extends JpaRepository<RentalRecord, Long> {
}