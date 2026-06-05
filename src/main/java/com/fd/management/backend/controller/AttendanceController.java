package com.fd.management.backend.controller;

import com.fd.management.backend.dto.AttendanceRequest;
import com.fd.management.backend.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(@RequestBody AttendanceRequest request) {
        try {
            return ResponseEntity.ok(attendanceService.checkIn(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/check-out")
    public ResponseEntity<?> checkOut(@RequestBody AttendanceRequest request) {
        try {
            return ResponseEntity.ok(attendanceService.checkOut(
                    request.getStaffId(),
                    request.getLatitude(),
                    request.getLongitude()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}