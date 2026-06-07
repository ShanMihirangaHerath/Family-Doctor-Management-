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

    // Postman එකෙන් Test කරන්න හදපු API එක
    @PostMapping("/send")
    public ResponseEntity<?> sendTestNotification(@RequestBody java.util.Map<String, String> request) {
        try {
            Long staffId = Long.parseLong(request.get("staffId"));
            notificationService.sendNotification(staffId, request.get("title"), request.get("body"));
            return ResponseEntity.ok("Notification push triggered successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}