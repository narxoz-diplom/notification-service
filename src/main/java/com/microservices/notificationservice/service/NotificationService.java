package com.microservices.notificationservice.service;

import com.microservices.notificationservice.mapper.NotificationMapper;
import com.microservices.notificationservice.model.Notification;
import com.microservices.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationTransactionalService notificationTransactionalService;

    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    public void processNotificationMessage(Map<String, Object> message) {
        try {
            var dto = notificationMapper.fromMessage(message);
            notificationTransactionalService.createNotification(dto);
            log.info("Notification processed successfully for user: {}", dto.getUserId());
        } catch (Exception e) {
            log.error("Error processing notification message", e);
        }
    }
}



