package com.property_management.property_prep.controller;

import com.property_management.property_prep.entity.User;
import com.property_management.property_prep.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // --- LOGIN ENDPOINT (unchanged) ---
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails.getUsername());

        return ResponseEntity.ok(Map.of("token", token));
    }

    // --- NEW: /me ENDPOINT (returns user details + subscription) ---
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<String, String> response = new HashMap<>();

        if (principal instanceof User) {
            User user = (User) principal;
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("role", user.getRole().name());
            response.put("subscription", user.getSubscription().name()); // <-- required for tier info
        } else {
            response.put("username", principal.toString());
            response.put("email", "N/A");
            response.put("role", "UNKNOWN");
            response.put("subscription", "UNKNOWN");
        }
        return ResponseEntity.ok(response);
    }
}