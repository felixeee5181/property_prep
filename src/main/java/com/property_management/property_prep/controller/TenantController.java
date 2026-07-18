package com.property_management.property_prep.controller;

import com.property_management.property_prep.entity.LeaseAgreement;
import com.property_management.property_prep.entity.Payment;
import com.property_management.property_prep.entity.User;
import com.property_management.property_prep.enums.RoleType;
import com.property_management.property_prep.repository.LeaseRepository;
import com.property_management.property_prep.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
public class TenantController {

    private final LeaseRepository leaseRepository;
    private final PaymentRepository paymentRepository;

    @GetMapping("/my-dashboard")
    public ResponseEntity<Map<String, Object>> getMyDashboard() {
        User tenant = getLoggedInTenant();
        List<LeaseAgreement> leases = leaseRepository.findByTenant(tenant);

        Map<String, Object> response = new HashMap<>();
        response.put("tenantName", tenant.getUsername());
        response.put("activeLeases", leases);

        // Get latest payment status for each lease
        if (!leases.isEmpty()) {
            LeaseAgreement activeLease = leases.get(0); // Simplified for demo
            List<Payment> payments = paymentRepository.findByLease(activeLease);
            response.put("payments", payments);
        }
        return ResponseEntity.ok(response);
    }

    private User getLoggedInTenant() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getRole() != RoleType.TENANT) {
            throw new RuntimeException("Access Denied: Tenants only");
        }
        return user;
    }
}