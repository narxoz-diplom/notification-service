package com.microservices.notificationservice.service;

import com.microservices.notificationservice.dto.CreateNotificationDto;
import com.microservices.notificationservice.mapper.NotificationMapper;
import com.microservices.notificationservice.model.Notification;
import com.microservices.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTransactionalService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Transactional
    public Notification createNotification(CreateNotificationDto dto) {
        log.info("Creating notification for user: {}, message: {}", dto.getUserId(), dto.getMessage());
        return notificationRepository.save(notificationMapper.toEntity(dto));
    }

    @Transactional
    public void markAsRead(Long notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to mark this notification as read");
        }

        notification.setRead(true);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void markAllAsRead(String userId) {
        List<Notification> unreadNotifications = getUnreadNotifications(userId);
        unreadNotifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

}
