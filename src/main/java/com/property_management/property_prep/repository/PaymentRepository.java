package com.property_management.property_prep.repository;

import com.property_management.property_prep.entity.LeaseAgreement;
import com.property_management.property_prep.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByLease(LeaseAgreement lease);

    // ✅ NEW: Find specific payment by lease, method, and status
    List<Payment> findByLeaseAndPaymentMethodAndStatus(LeaseAgreement lease, String paymentMethod, String status);
}