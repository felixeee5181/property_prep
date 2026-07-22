package com.property_management.property_prep.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateManagerRequest {
    @NotBlank private String username;
    @NotBlank private String password;
    @Email @NotBlank private String email;
}