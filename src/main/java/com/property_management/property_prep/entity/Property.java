package com.property_management.property_prep.entity;


import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "properties")
@Data
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;
    private String city;
    private Double monthlyRent;
    private boolean isOccupied; // true if someone is living there

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager; // The LANDLORD who owns this
}