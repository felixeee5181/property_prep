package com.property_management.property_prep.controller;

import com.property_management.property_prep.entity.Property;
import com.property_management.property_prep.entity.User;
import com.property_management.property_prep.enums.RoleType;
import com.property_management.property_prep.repository.PropertyRepository;
import com.property_management.property_prep.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    // --- Existing GET endpoints ---
    @GetMapping("/managers")
    public ResponseEntity<List<User>> getAllManagers() {
        checkAdmin();
        return ResponseEntity.ok(userRepository.findByRole(RoleType.LANDLORD));
    }

    @GetMapping("/vacant-properties")
    public ResponseEntity<List<Property>> getVacantProperties() {
        checkAdmin();
        return ResponseEntity.ok(propertyRepository.findByIsOccupiedFalse());
    }

    // --- NEW: Update a manager's subscription tier ---
    @PutMapping("/managers/{managerId}/subscription")
    public ResponseEntity<String> updateSubscription(
            @PathVariable Long managerId,
            @RequestParam User.SubscriptionTier tier
    ) {
        User admin = getLoggedInAdmin(); // ensure only admin can do this

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        // Ensure the target user is actually a LANDLORD
        if (manager.getRole() != RoleType.LANDLORD) {
            return ResponseEntity.badRequest().body("User is not a manager (LANDLORD)");
        }

        manager.setSubscription(tier);
        userRepository.save(manager);
        return ResponseEntity.ok("Subscription updated to " + tier);
    }

    // --- Helper methods ---
    private void checkAdmin() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getRole() != RoleType.ADMIN) {
            throw new RuntimeException("Access Denied: Admins only");
        }
    }

    private User getLoggedInAdmin() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getRole() != RoleType.ADMIN) {
            throw new RuntimeException("Access Denied: Admins only");
        }
        return user;
    }
}