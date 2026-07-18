package com.property_management.property_prep.controller;

import com.property_management.property_prep.dto.RegisterRequest;
import com.property_management.property_prep.entity.User;
import com.property_management.property_prep.enums.RoleType;
import com.property_management.property_prep.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ Registration endpoint (kept exactly as you had it)
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(RoleType.TENANT);
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    // ✅ The MISSING endpoint that returns the current user's role
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<String, String> response = new HashMap<>();

        if (principal instanceof User) {
            User user = (User) principal;
            response.put("username", user.getUsername());
            response.put("role", user.getRole().name());
        } else {
            response.put("username", principal.toString());
            response.put("role", "UNKNOWN");
        }
        return ResponseEntity.ok(response);
    }
}