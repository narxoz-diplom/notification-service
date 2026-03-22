package com.microservices.notificationservice.service;

import com.microservices.notificationservice.dto.CreateNewsRequest;
import com.microservices.notificationservice.dto.NewsArticleDto;
import com.microservices.notificationservice.dto.UpdateNewsRequest;
import com.microservices.notificationservice.model.NewsArticle;
import com.microservices.notificationservice.repository.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsArticleService {

    private final NewsArticleRepository newsArticleRepository;
    private final NewsMinioStorage newsMinioStorage;

    public List<NewsArticleDto> listAll() {
        return newsArticleRepository.findAllByOrderByPublishedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    public NewsArticleDto getById(Long id) {
        NewsArticle article = newsArticleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "News not found"));
        return toDto(article);
    }

    @Transactional
    public NewsArticleDto create(CreateNewsRequest request, Jwt jwt) {
        validateCreate(request);
        String objectName;
        try {
            objectName = newsMinioStorage.uploadTextContent(request.getContent());
        } catch (Exception e) {
            log.error("Failed to upload news body to MinIO", e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not store news content");
        }
        NewsArticle entity = NewsArticle.builder()
                .title(request.getTitle().trim())
                .shortDescription(request.getShortDescription().trim())
                .contentObjectName(objectName)
                .authorId(jwt.getSubject())
                .authorName(resolveAuthorName(jwt))
                .build();
        entity = newsArticleRepository.save(entity);
        return toDto(entity);
    }

    @Transactional
    public NewsArticleDto update(Long id, UpdateNewsRequest request) {
        NewsArticle article = newsArticleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "News not found"));
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is required");
        }
        if (request.getShortDescription() == null || request.getShortDescription().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "shortDescription is required");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "content is required");
        }
        String oldKey = article.getContentObjectName();
        String newKey;
        try {
            newKey = newsMinioStorage.uploadTextContent(request.getContent());
        } catch (Exception e) {
            log.error("Failed to upload updated news body to MinIO", e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not store news content");
        }
        article.setTitle(request.getTitle().trim());
        article.setShortDescription(request.getShortDescription().trim());
        article.setContentObjectName(newKey);
        newsArticleRepository.save(article);
        newsMinioStorage.deleteObject(oldKey);
        return toDto(article);
    }

    @Transactional
    public void delete(Long id) {
        NewsArticle article = newsArticleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "News not found"));
        newsMinioStorage.deleteObject(article.getContentObjectName());
        newsArticleRepository.delete(article);
    }

    private void validateCreate(CreateNewsRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is required");
        }
        if (request.getShortDescription() == null || request.getShortDescription().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "shortDescription is required");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "content is required");
        }
    }

    private NewsArticleDto toDto(NewsArticle article) {
        String content;
        try {
            content = newsMinioStorage.readTextContent(article.getContentObjectName());
        } catch (IOException e) {
            log.error("Missing or unreadable MinIO object for news {}", article.getId(), e);
            content = "";
        }
        return NewsArticleDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .shortDescription(article.getShortDescription())
                .content(content)
                .publishedAt(article.getPublishedAt())
                .authorName(article.getAuthorName())
                .build();
    }

    private static String resolveAuthorName(Jwt jwt) {
        String preferred = jwt.getClaimAsString("preferred_username");
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        String name = jwt.getClaimAsString("name");
        if (name != null && !name.isBlank()) {
            return name;
        }
        return jwt.getSubject() != null ? jwt.getSubject() : "admin";
    }
}
