package com.microservices.notificationservice.mapper;

import com.microservices.notificationservice.dto.CreateNotificationDto;
import com.microservices.notificationservice.model.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "read", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "type", defaultValue = "GENERAL")
    Notification toEntity(CreateNotificationDto dto);

    default CreateNotificationDto fromMessage(Map<String, Object> message) {
        return CreateNotificationDto.builder()
                .userId((String) message.get("userId"))
                .message((String) message.get("message"))
                .type((String) message.getOrDefault("type", "GENERAL"))
                .build();
    }
}
