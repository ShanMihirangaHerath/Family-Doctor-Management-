package com.fd.management.backend.service;

import com.fd.management.backend.dto.AttendanceRequest;
import com.fd.management.backend.entity.Attendance;
import com.fd.management.backend.entity.OfficeLocation;
import com.fd.management.backend.entity.Staff;
import com.fd.management.backend.repository.AttendanceRepository;
import com.fd.management.backend.repository.OfficeLocationRepository;
import com.fd.management.backend.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StaffRepository staffRepository;
    private final OfficeLocationRepository officeLocationRepository;

    // 1. Check-In කිරීම
    public Attendance checkIn(AttendanceRequest request) {
        Staff staff = staffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new RuntimeException("Staff member not found"));

        OfficeLocation office = officeLocationRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Office location has not been set by Admin yet!"));

        if (request.getLatitude() != null && request.getLongitude() != null) {
            double distance = calculateDistance(
                    request.getLatitude(), request.getLongitude(),
                    office.getLatitude(), office.getLongitude()
            );

            if (distance > office.getRadiusMeters()) {
                throw new RuntimeException("Check-in failed! You are " + Math.round(distance) + " meters away from the office. Must be within " + office.getRadiusMeters() + "m.");
            }
        } else {
            throw new RuntimeException("Location data is required for check-in!");
        }

        List<Attendance> records = attendanceRepository.findByStaffId(staff.getId());
        boolean alreadyCheckedIn = records.stream()
                .anyMatch(a -> a.getCheckOutTime() == null);
        if (alreadyCheckedIn) {
            throw new RuntimeException("You are already checked in!");
        }

        Attendance attendance = new Attendance();
        attendance.setStaff(staff);
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setCheckInLatitude(request.getLatitude());
        attendance.setCheckInLongitude(request.getLongitude());
        attendance.setQrUsed(request.getScannedQr() != null && !request.getScannedQr().isEmpty());
        attendance.setGeofenceUsed(true);
        attendance.setStatus("PRESENT");

        return attendanceRepository.save(attendance);
    }

    // 2. Check-Out කිරීම
    public Attendance checkOut(Long staffId, Double currentLat, Double currentLon) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff member not found"));

        OfficeLocation office = officeLocationRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Office location has not been set by Admin yet!"));

        if (currentLat != null && currentLon != null) {
            double distance = calculateDistance(currentLat, currentLon, office.getLatitude(), office.getLongitude());
            if (distance > office.getRadiusMeters()) {
                throw new RuntimeException("Check-out failed! You are " + Math.round(distance) + " meters away. Please go back to the office to check out.");
            }
        } else {
            throw new RuntimeException("Location data is required for check-out!");
        }

        List<Attendance> records = attendanceRepository.findByStaffId(staffId);
        Attendance latestAttendance = records.stream()
                .filter(a -> a.getCheckOutTime() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active check-in found to check out from"));

        latestAttendance.setCheckOutTime(LocalDateTime.now());
        latestAttendance.setCheckOutLatitude(currentLat);
        latestAttendance.setCheckOutLongitude(currentLon);

        return attendanceRepository.save(latestAttendance);
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceInKm = EARTH_RADIUS * c;

        return distanceInKm * 1000;
    }
}