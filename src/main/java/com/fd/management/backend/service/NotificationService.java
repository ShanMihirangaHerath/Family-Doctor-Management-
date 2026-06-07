package com.fd.management.backend.service;

import com.fd.management.backend.entity.Notification;
import com.fd.management.backend.entity.Staff;
import com.fd.management.backend.repository.NotificationRepository;
import com.fd.management.backend.repository.StaffRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final StaffRepository staffRepository;

    public void sendNotification(Long staffId, String title, String body) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(body);
        notification.setStaff(staff);
        notificationRepository.save(notification);

        if (staff.getFcmToken() != null && !staff.getFcmToken().isEmpty()) {
            try {
                Message message = Message.builder()
                        .setToken(staff.getFcmToken())
                        .setNotification(com.google.firebase.messaging.Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .build();

                String response = FirebaseMessaging.getInstance().send(message);
                System.out.println("Successfully sent message: " + response);
            } catch (Exception e) {
                System.out.println("Failed to send push notification: " + e.getMessage());
            }
        }
    }
}