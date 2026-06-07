package com.fd.management.backend.repository;

import com.fd.management.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByStaffIdOrderByCreatedAtDesc(Long staffId);
}