package com.property_management.property_prep.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank private String username;
    @NotBlank private String password;
    @Email @NotBlank private String email;
    private String phone;

  //  private String role; // optional – default to TENANT
}