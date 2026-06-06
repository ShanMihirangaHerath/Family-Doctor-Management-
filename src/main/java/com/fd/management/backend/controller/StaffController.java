package com.fd.management.backend.controller;

import com.fd.management.backend.dto.StaffRequest;
import com.fd.management.backend.entity.Staff;
import com.fd.management.backend.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @PostMapping("/add")
    public ResponseEntity<?> addStaff(@RequestBody StaffRequest request) {
        try {
            return ResponseEntity.ok(staffService.addStaff(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to add staff: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Staff>> getAllStaff() {
        return ResponseEntity.ok(staffService.getAllStaff());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            return ResponseEntity.ok(staffService.loginByEmail(credentials.get("email")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}