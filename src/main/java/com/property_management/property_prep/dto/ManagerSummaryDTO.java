package com.property_management.property_prep.dto;

public record ManagerSummaryDTO(
        Long id,
        String username,
        String email,
        String subscription,
        long propertyCount
) {}