package com.fd.management.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "staff_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- පරණ Basic Info ටික ---
    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String phone;

    @Column(nullable = false)
    private String role;

    @Column(unique = true)
    private String qrCodeString;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    // --- අලුතෙන් එකතු කරපු Bio-data ටික ---
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "nic", unique = true)
    private String nic;

    @Column(name = "mobile_no")
    private String mobileNo;

    @Column(name = "whatsapp_no")
    private String whatsappNo;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    // --- අලුතෙන් එකතු කරපු Bank Account Details ---
    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "cv_url")
    private String cvUrl;

    // --- Relational Table Mapping (Emergency Contacts) ---
    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmergencyContact> emergencyContacts = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}