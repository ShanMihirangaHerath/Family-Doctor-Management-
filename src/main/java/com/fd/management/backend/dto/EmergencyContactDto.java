package com.fd.management.backend.dto;

import lombok.Data;

@Data
public class EmergencyContactDto {
    private String name;
    private String relationship;
    private String contactNumber;
}