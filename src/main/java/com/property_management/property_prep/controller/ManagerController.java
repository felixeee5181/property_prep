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
import org.springframework.security.access.prepost.PreAuthorize;
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
@PreAuthorize("hasRole('LANDLORD')") // 🔒 Only Managers can use these endpoints
public class ManagerController {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final LeaseRepository leaseRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. Create a Tenant
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

    // 2. Create a Property (WITH PROPERTY LIMIT ENFORCEMENT)
    @PostMapping("/create-property")
    public ResponseEntity<String> createProperty(@RequestBody Property property) {
        User manager = getLoggedInManager();

        // 🔒 Check property limit based on subscription
        int currentCount = propertyRepository.countByManager(manager);
        int maxAllowed = switch (manager.getSubscription()) {
            case BASIC -> 5;
            case PRO -> 20;
            case PREMIUM -> 100;
        };
        if (currentCount >= maxAllowed) {
            return ResponseEntity.status(403).body(
                    "Property limit reached (" + currentCount + "/" + maxAllowed + "). " +
                            "Please ask the Admin to upgrade your subscription."
            );
        }

        property.setManager(manager);
        property.setOccupied(false);
        propertyRepository.save(property);
        return ResponseEntity.ok("Property created successfully");
    }

    // 3. Get My Properties
    @GetMapping("/my-properties")
    public ResponseEntity<List<Property>> getMyProperties() {
        User manager = getLoggedInManager();
        return ResponseEntity.ok(propertyRepository.findByManager(manager));
    }

    // 4. Get My Tenants
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

    // 5. Assign Lease to a Tenant (Moves them in)
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

    // 6. Get My Payments (Income)
    @GetMapping("/my-payments")
    public ResponseEntity<List<Payment>> getMyPayments() {
        User manager = getLoggedInManager();
        List<Property> myProps = propertyRepository.findByManager(manager);
        List<LeaseAgreement> leases = leaseRepository.findByPropertyIn(myProps);
        List<Payment> payments = new ArrayList<>();
        for (LeaseAgreement lease : leases) {
            payments.addAll(paymentRepository.findByLease(lease));
        }
        return ResponseEntity.ok(payments);
    }

    // --- Helper to get the logged-in Manager ---
    private User getLoggedInManager() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getRole() != RoleType.LANDLORD) {
            throw new RuntimeException("Access Denied: Managers only");
        }
        return user;
    }
}