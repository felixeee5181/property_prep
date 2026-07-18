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

    private void checkAdmin() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getRole() != RoleType.ADMIN) {
            throw new RuntimeException("Access Denied: Admins only");
        }
    }
}