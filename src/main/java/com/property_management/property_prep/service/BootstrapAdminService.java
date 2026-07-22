package com.property_management.property_prep.service;

import com.property_management.property_prep.entity.User;
import com.property_management.property_prep.enums.RoleType;
import com.property_management.property_prep.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BootstrapAdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.password:Help@5181}")
    private String adminPassword;

    @Value("${admin.email:admin@propertyprep.com}")
    private String adminEmail;

    @EventListener(ApplicationReadyEvent.class)
    public void createSystemAdminIfMissing() {
        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setEmail(adminEmail);
            admin.setRole(RoleType.ADMIN);
            admin.setSubscription(User.SubscriptionTier.BASIC); // default, but admin doesn't matter
            userRepository.save(admin);
            log.info("✅ System admin created with username: {}", adminUsername);
        } else {
            log.info("✅ System admin already exists: {}", adminUsername);
        }
    }
}