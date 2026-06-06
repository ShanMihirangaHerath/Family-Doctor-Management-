package com.fd.management.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class StaffRequest {
    // Basic & Bio Data
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String phone;
    private String role;
    private String nic;
    private String mobileNo;
    private String whatsappNo;
    private String address;

    // Bank Details
    private String bankName;
    private String branchName;
    private String accountName;
    private String accountNumber;

    // Emergency Contacts List
    private List<EmergencyContactDto> emergencyContacts;
}