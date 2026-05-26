package com.microservices.notificationservice.controller;

import com.microservices.notificationservice.model.Notification;
import com.microservices.notificationservice.service.NotificationService;
import com.microservices.notificationservice.service.NotificationTransactionalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationTransactionalService notificationTransactionalService;

    @GetMapping
    public ResponseEntity<List<Notification>> getAll(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                notificationService.getUserNotifications(jwt.getSubject())
        );
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnread(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                notificationTransactionalService.getUnreadNotifications(jwt.getSubject())
        );
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal Jwt jwt) {
        long count = notificationService.getUnreadCount(jwt.getSubject());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        notificationTransactionalService.markAsRead(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal Jwt jwt) {
        notificationTransactionalService.markAllAsRead(jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}

