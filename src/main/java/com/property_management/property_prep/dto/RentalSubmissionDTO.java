package com.property_management.property_prep.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RentalSubmissionDTO {
    @NotBlank(message = "House number is required")
    private String houseNumber;

    @NotBlank(message = "Tenant name is required")
    private String tenantName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Tenant email is required")
    private String tenantEmail;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
    private String tenantPhone;

    @NotNull(message = "Payment status is required")
    private PaymentStatus paymentStatus;

    public enum PaymentStatus {
        PAID, NOT_PAID, PARTIALLY_PAID
    }
}