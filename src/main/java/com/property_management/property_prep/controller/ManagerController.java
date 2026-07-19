package com.property_management.property_prep.controller;

import com.property_management.property_prep.dto.RegisterRequest;
import com.property_management.property_prep.entity.LeaseAgreement;
import com.property_management.property_prep.entity.Payment;
import com.property_management.property_prep.entity.Property;
import com.property_management.property_prep.entity.User;
import com.property_management.property_prep.enums.RoleType;
import com.property_management.property_prep.repository.LeaseRepository;
import com.property_management.property_prep.repository.PaymentRepository;
import com.property_management.property_prep.repository.PropertyRepository;
import com.property_management.property_prep.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final LeaseRepository leaseRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/create-tenant")
    public ResponseEntity<String> createTenant(@RequestBody RegisterRequest request) {
        User manager = getLoggedInManager();

        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(409).body("Username taken");
        }

        User tenant = new User();
        tenant.setUsername(request.getUsername());
        tenant.setPassword(passwordEncoder.encode(request.getPassword()));
        tenant.setEmail(request.getEmail());
        tenant.setRole(RoleType.TENANT);
        userRepository.save(tenant);

        return ResponseEntity.ok("Tenant created successfully");
    }

    @PostMapping("/create-property")
    public ResponseEntity<String> createProperty(@RequestBody Property property) {
        User manager = getLoggedInManager();
        property.setManager(manager);
        property.setOccupied(false);
        propertyRepository.save(property);
        return ResponseEntity.ok("Property created successfully");
    }

    @GetMapping("/my-properties")
    public ResponseEntity<List<Property>> getMyProperties() {
        User manager = getLoggedInManager();
        return ResponseEntity.ok(propertyRepository.findByManager(manager));
    }

    @GetMapping("/my-tenants")
    public ResponseEntity<List<User>> getMyTenants() {
        User manager = getLoggedInManager();
        List<Property> myProps = propertyRepository.findByManager(manager);
        List<LeaseAgreement> leases = leaseRepository.findByPropertyIn(myProps);
        List<User> tenants = leases.stream()
                .map(LeaseAgreement::getTenant)
                .distinct()
                .collect(Collectors.toList());
        return ResponseEntity.ok(tenants);
    }

    @PostMapping("/assign-lease")
    public ResponseEntity<String> assignLease(
            @RequestParam("tenantId") Long tenantId,
            @RequestParam("propertyId") Long propertyId) {

        User manager = getLoggedInManager();

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        if (!property.getManager().getId().equals(manager.getId())) {
            throw new RuntimeException("You do not own this property");
        }

        User tenant = userRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        LeaseAgreement lease = new LeaseAgreement();
        lease.setTenant(tenant);
        lease.setProperty(property);
        lease.setStartDate(LocalDate.now());
        lease.setEndDate(LocalDate.now().plusMonths(12));
        lease.setActive(true);
        leaseRepository.save(lease);

        property.setOccupied(true);
        propertyRepository.save(property);

        Payment payment = new Payment();
        payment.setLease(lease);
        payment.setAmount(property.getMonthlyRent());
        payment.setDueDate(LocalDate.now().plusDays(30));
        payment.setStatus("PENDING");
        paymentRepository.save(payment);

        return ResponseEntity.ok("Lease assigned successfully! Tenant moved in.");
    }
    @GetMapping("/my-payments")
    public ResponseEntity<List<Payment>> getMyPayments() {
        User manager = getLoggedInManager();
        // 1. Get all properties owned by this manager
        List<Property> myProps = propertyRepository.findByManager(manager);
        // 2. Get all leases for those properties
        List<LeaseAgreement> leases = leaseRepository.findByPropertyIn(myProps);
        // 3. Extract all payments for those leases
        List<Payment> payments = new ArrayList<>();
        for (LeaseAgreement lease : leases) {
            payments.addAll(paymentRepository.findByLease(lease));
        }
        return ResponseEntity.ok(payments);
    }
    private User getLoggedInManager() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getRole() != RoleType.LANDLORD) {
            throw new RuntimeException("Access Denied: Managers only");
        }
        return user;
    }
}