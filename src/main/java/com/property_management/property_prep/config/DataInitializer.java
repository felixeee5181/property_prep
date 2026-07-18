package com.property_management.property_prep.config;

import com.property_management.property_prep.entity.User;
import com.property_management.property_prep.enums.RoleType;
import com.property_management.property_prep.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initUsers() {
        return args -> {
            System.out.println(">>> DataInitializer running...");

            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@propertyprep.com");
                admin.setRole(RoleType.ADMIN);
                userRepository.save(admin);
                System.out.println(">>> Created admin user");
            }

            if (!userRepository.existsByUsername("manager")) {
                User manager = new User();
                manager.setUsername("manager");
                manager.setPassword(passwordEncoder.encode("pass123"));
                manager.setEmail("manager@prep.com");
                manager.setRole(RoleType.LANDLORD);
                userRepository.save(manager);
                System.out.println(">>> Created manager user");
            }

            if (!userRepository.existsByUsername("tenant1")) {
                User tenant = new User();
                tenant.setUsername("tenant1");
                tenant.setPassword(passwordEncoder.encode("pass123"));
                tenant.setEmail("tenant@prep.com");
                tenant.setRole(RoleType.TENANT);
                userRepository.save(tenant);
                System.out.println(">>> Created tenant1 user");
            }
        };
    }
}