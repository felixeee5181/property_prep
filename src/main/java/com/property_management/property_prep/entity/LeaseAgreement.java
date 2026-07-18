package com.property_management.property_prep.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "leases")
@Data
public class LeaseAgreement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private User tenant;

    @ManyToOne
    @JoinColumn(name = "property_id")
    private Property property;

    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isActive;
}