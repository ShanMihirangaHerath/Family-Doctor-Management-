package com.fd.management.backend.controller;

import com.fd.management.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping("/{staffId}")
    public ResponseEntity<?> getNotifications(@PathVariable Long staffId) {
        try {
            return ResponseEntity.ok(notificationRepository.findByStaffIdOrderByCreatedAtDesc(staffId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}