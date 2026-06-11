package com.fd.management.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class StaffRequest {
    private String email;
    private String password;
    private String firstName;
    private String middleName;
    private String lastName;
    private String nic;
    private String phone;
    private String mobileNo;
    private String whatsappNo;
    private String address;
    private String bankName;
    private String branchName;
    private String accountName;
    private String accountNumber;
    private String role;
    private List<EmergencyContactDto> emergencyContacts;
}