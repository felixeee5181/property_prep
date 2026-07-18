package com.property_management.property_prep.repository;

import com.property_management.property_prep.entity.User;
import com.property_management.property_prep.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByRole(RoleType role);  // <-- ADD THIS
}