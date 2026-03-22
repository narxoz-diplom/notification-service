package com.microservices.notificationservice.dto;

import lombok.Data;

@Data
public class CreateNewsRequest {
    private String title;
    private String shortDescription;
    private String content;
}
