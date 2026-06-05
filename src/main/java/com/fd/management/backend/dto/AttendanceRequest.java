package com.fd.management.backend.dto;

import lombok.Data;

@Data
public class AttendanceRequest {
    private Long staffId;
    private Double latitude;
    private Double longitude;
    private String scannedQr;
}