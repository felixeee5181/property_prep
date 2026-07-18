package com.property_management.property_prep.entity;

import com.property_management.property_prep.dto.RentalSubmissionDTO;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "rental_records")
@Data
public class RentalRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String houseNumber;

    @Column(nullable = false)
    private String tenantName;

    @Column(nullable = false)
    private String tenantEmail;

    private String tenantPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalSubmissionDTO.PaymentStatus paymentStatus;

    @CreationTimestamp
    private LocalDateTime submittedAt;
}