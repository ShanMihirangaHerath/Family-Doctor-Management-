package com.fd.management.backend.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fd.management.backend.config.JwtUtil;
import com.fd.management.backend.service.CloudinaryService;
import com.fd.management.backend.dto.StaffRequest;
import com.fd.management.backend.dto.BankDetailsRequest;
import com.fd.management.backend.dto.EmergencyContactDto;
import com.fd.management.backend.entity.Staff;
import com.fd.management.backend.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;
    private final JwtUtil jwtUtil;
    private final CloudinaryService cloudinaryService;

    @PostMapping("/add")
    public ResponseEntity<?> addStaff(@RequestBody StaffRequest request) {
        try {
            System.out.println("==== Adding: " + request.getEmail() + " ====");

            // CV එක පස්සේ අප්ඩේට් කරන නිසා මෙතනදි null පාස් කරනවා
            Staff savedStaff = staffService.addStaff(request, null);

            System.out.println("==== Done! ====");
            return ResponseEntity.ok(savedStaff);
        } catch (Exception e) {
            System.out.println("==== Error:  ====");
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", "Failed: " + e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Staff>> getAllStaff() {
        return ResponseEntity.ok(staffService.getAllStaff());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");

            Staff staff = staffService.loginUser(email, password);

            String token = jwtUtil.generateToken(staff.getEmail(), staff.getRole(), staff.getFullName());

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "role", staff.getRole(),
                    "name", staff.getFullName()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStaffProfile(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(staffService.getStaffById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/bank")
    public ResponseEntity<?> updateBankDetails(@PathVariable Long id, @RequestBody BankDetailsRequest request) {
        try {
            return ResponseEntity.ok(staffService.updateBankDetails(id, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to update bank details: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/contact")
    public ResponseEntity<?> addEmergencyContact(@PathVariable Long id, @RequestBody EmergencyContactDto request) {
        try {
            return ResponseEntity.ok(staffService.addEmergencyContact(id, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to add contact: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/fcm-token")
    public ResponseEntity<?> updateFcmToken(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            staffService.updateFcmToken(id, request.get("token"));
            return ResponseEntity.ok(Map.of("message", "Token updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error updating token: " + e.getMessage()));
        }
    }
}