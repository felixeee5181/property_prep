package com.property_management.property_prep.controller;

import com.property_management.property_prep.dto.RentalSubmissionDTO;
import com.property_management.property_prep.service.RentalSubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rental-submissions")
@CrossOrigin(origins = "http://localhost:63342", allowCredentials = "true")
@RequiredArgsConstructor
public class RentalSubmissionController {
    private final RentalSubmissionService service;

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitRentalData(@Valid @RequestBody RentalSubmissionDTO dto) {
        service.processSubmission(dto);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Rental data submitted successfully. Landlord has been notified.");
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = service.getPaymentStats();
        return ResponseEntity.ok(stats);
    }
}