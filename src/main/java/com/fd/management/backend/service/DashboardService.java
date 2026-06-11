package com.fd.management.backend.service;

import com.fd.management.backend.dto.DashboardStatsDto;
import com.fd.management.backend.entity.Attendance;
import com.fd.management.backend.entity.LeaveRequest;
import com.fd.management.backend.entity.Staff;
import com.fd.management.backend.repository.AttendanceRepository;
import com.fd.management.backend.repository.LeaveRequestRepository;
import com.fd.management.backend.repository.OfficeLocationRepository;
import com.fd.management.backend.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final StaffRepository staffRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRepository;
    private final OfficeLocationRepository officeLocationRepository;

    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();
        LocalDate today = LocalDate.now();

        List<Staff> allStaff = staffRepository.findAll();
        stats.setTotalStaff(allStaff.size());

        Map<String, Long> roleCounts = allStaff.stream()
                .collect(Collectors.groupingBy(Staff::getRole, Collectors.counting()));

        List<Map<String, Object>> roleDist = new ArrayList<>();
        roleCounts.forEach((role, count) -> {
            roleDist.add(Map.of("name", role, "value", count));
        });
        stats.setRoleDistribution(roleDist);

        // 2. Active Branches
        stats.setActiveBranches(officeLocationRepository.count());

        // 3. Present Today
        List<Attendance> allAttendances = attendanceRepository.findAll();
        long presentToday = allAttendances.stream()
                .filter(a -> a.getCheckInTime().toLocalDate().equals(today))
                .map(a -> a.getStaff().getId())
                .distinct()
                .count();
        stats.setPresentToday(presentToday);

        // 4. On Leave Today
        List<LeaveRequest> allLeaves = leaveRepository.findAll();
        long onLeaveToday = allLeaves.stream()
                .filter(l -> "APPROVED".equals(l.getStatus()) &&
                        !today.isBefore(l.getStartDate()) &&
                        !today.isAfter(l.getEndDate()))
                .map(l -> l.getStaff().getId())
                .distinct()
                .count();
        stats.setOnLeaveToday(onLeaveToday);

        // 5. Attendance Trends (Last 7 Days)
        List<Map<String, Object>> trends = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long present = allAttendances.stream()
                    .filter(a -> a.getCheckInTime().toLocalDate().equals(date))
                    .map(a -> a.getStaff().getId())
                    .distinct()
                    .count();

            long onLeave = allLeaves.stream()
                    .filter(l -> "APPROVED".equals(l.getStatus()) &&
                            !date.isBefore(l.getStartDate()) &&
                            !date.isAfter(l.getEndDate()))
                    .map(l -> l.getStaff().getId())
                    .distinct()
                    .count();

            long absent = stats.getTotalStaff() - present - onLeave;
            if (absent < 0) absent = 0;

            trends.add(Map.of(
                    "name", date.format(formatter),
                    "present", present,
                    "absent", absent
            ));
        }
        stats.setAttendanceTrends(trends);

        return stats;
    }
}