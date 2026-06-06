package com.fd.management.backend.controller;

import com.fd.management.backend.dto.LeaveDto;
import com.fd.management.backend.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leave")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveRequestService leaveService;

    @PostMapping("/apply/{staffId}")
    public ResponseEntity<?> applyLeave(@PathVariable Long staffId, @RequestBody LeaveDto request) {
        try {
            return ResponseEntity.ok(leaveService.applyLeave(staffId, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to apply leave: " + e.getMessage());
        }
    }

    @GetMapping("/history/{staffId}")
    public ResponseEntity<?> getLeaveHistory(@PathVariable Long staffId) {
        try {
            return ResponseEntity.ok(leaveService.getMyLeaves(staffId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}