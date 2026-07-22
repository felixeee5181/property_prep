package com.property_management.property_prep.controller;

import com.property_management.property_prep.dto.CreateManagerRequest;
import com.property_management.property_prep.dto.ManagerSummaryDTO;
import com.property_management.property_prep.dto.UserProfileDTO;
import com.property_management.property_prep.entity.User;
import com.property_management.property_prep.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // 🔒 Only Admins can use these endpoints
public class AdminController {

    private final AdminService adminService;

    // 1. Create a new Manager (Landlord)
    @PostMapping("/managers")
    public ResponseEntity<String> createManager(@RequestBody CreateManagerRequest request) {
        adminService.createManager(request);
        return ResponseEntity.ok("Manager created successfully.");
    }

    // 2. Get list of all Managers with their property counts
    @GetMapping("/managers")
    public ResponseEntity<List<ManagerSummaryDTO>> getAllManagers() {
        return ResponseEntity.ok(adminService.getAllManagers());
    }

    // 3. Get ALL users (for Admin dashboard)
    @GetMapping("/all-users")
    public ResponseEntity<List<UserProfileDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    // 4. Update a manager's subscription tier (Gives admin consent to property limits)
    @PutMapping("/managers/{managerId}/subscription")
    public ResponseEntity<String> updateSubscription(
            @PathVariable Long managerId,
            @RequestParam User.SubscriptionTier tier
    ) {
        adminService.updateManagerSubscription(managerId, tier);
        return ResponseEntity.ok("Subscription updated to " + tier);
    }
}