package com.fd.management.backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class LeaveDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
}