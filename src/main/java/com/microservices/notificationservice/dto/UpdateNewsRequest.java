package com.microservices.notificationservice.dto;

import lombok.Data;

@Data
public class UpdateNewsRequest {
    private String title;
    private String shortDescription;
    private String content;
}
