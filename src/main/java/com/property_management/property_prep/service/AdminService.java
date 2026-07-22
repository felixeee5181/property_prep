package com.property_management.property_prep.service;

import com.property_management.property_prep.dto.CreateManagerRequest;
import com.property_management.property_prep.dto.ManagerSummaryDTO;
import com.property_management.property_prep.dto.UserProfileDTO;
import com.property_management.property_prep.entity.User;
import com.property_management.property_prep.enums.RoleType;
import com.property_management.property_prep.repository.PropertyRepository;
import com.property_management.property_prep.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username:admin}")
    private String systemAdminUsername;

    // 1. Create a new Manager (Landlord)
    public User createManager(CreateManagerRequest request) {
        // Use getUsername() because the DTO is a @Data class
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User manager = new User();
        manager.setUsername(request.getUsername());
        manager.setPassword(passwordEncoder.encode(request.getPassword()));
        manager.setEmail(request.getEmail());
        manager.setRole(RoleType.LANDLORD);
        manager.setSubscription(User.SubscriptionTier.BASIC);
        return userRepository.save(manager);
    }

    // 2. Get all Managers with property counts
    public List<ManagerSummaryDTO> getAllManagers() {
        List<User> managers = userRepository.findByRole(RoleType.LANDLORD);
        return managers.stream().map(m -> {
            long propCount = propertyRepository.countByManager(m);
            return new ManagerSummaryDTO(
                    m.getId(),
                    m.getUsername(),
                    m.getEmail(),
                    m.getSubscription().name(),
                    propCount
            );
        }).collect(Collectors.toList());
    }

    // 3. Get ALL platform users
    public List<UserProfileDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserProfileDTO::new)
                .collect(Collectors.toList());
    }

    // 4. Update a manager's subscription (Property limit controlled here)
    public void updateManagerSubscription(Long managerId, User.SubscriptionTier tier) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        // 🔒 Protect the system admin
        if (manager.getUsername().equals(systemAdminUsername)) {
            throw new RuntimeException("Cannot modify the system administrator account.");
        }

        if (manager.getRole() != RoleType.LANDLORD) {
            throw new RuntimeException("User is not a manager (LANDLORD).");
        }

        manager.setSubscription(tier);
        userRepository.save(manager);
    }
}