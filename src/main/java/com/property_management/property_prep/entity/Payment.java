package com.property_management.property_prep.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lease_id")
    private LeaseAgreement lease;

    private Double amount;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private String status; // PENDING, PAID, OVERDUE
}