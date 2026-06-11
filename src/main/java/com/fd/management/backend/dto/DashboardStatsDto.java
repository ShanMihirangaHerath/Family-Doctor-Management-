package com.fd.management.backend.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardStatsDto {
    private long totalStaff;
    private long presentToday;
    private long onLeaveToday;
    private long activeBranches;
    private List<Map<String, Object>> roleDistribution;
    private List<Map<String, Object>> attendanceTrends;
}