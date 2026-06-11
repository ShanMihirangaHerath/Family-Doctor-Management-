package com.fd.management.backend.controller;

import com.fd.management.backend.repository.NotificationRepository;
import com.fd.management.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @GetMapping("/{staffId}")
    public ResponseEntity<?> getNotifications(@PathVariable Long staffId) {
        try {
            return ResponseEntity.ok(notificationRepository.findByStaffIdOrderByCreatedAtDesc(staffId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendTestNotification(@RequestBody Map<String, String> request) {
        try {
            Long staffId = Long.parseLong(request.get("staffId"));
            notificationService.sendNotification(staffId, request.get("title"), request.get("body"));
            return ResponseEntity.ok("Notification push triggered successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}