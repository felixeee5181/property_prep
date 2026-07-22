package com.property_management.property_prep.dto;

import com.property_management.property_prep.entity.User;

public class UserProfileDTO {
    public String username;
    public String email;
    public String role;
    public String subscription;

    public UserProfileDTO(User user) {
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole().name();
        this.subscription = user.getSubscription().name();
    }
}