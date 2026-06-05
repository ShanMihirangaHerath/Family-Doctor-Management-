package com.fd.management.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "office_locations")
@Data
public class OfficeLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String branchName;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private Double radiusMeters = 50.0;
}