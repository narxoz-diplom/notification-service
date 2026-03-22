package com.microservices.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticleDto {
    private Long id;
    private String title;
    private String shortDescription;
    private String content;
    private Instant publishedAt;
    private String authorName;
}
