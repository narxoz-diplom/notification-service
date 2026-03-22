package com.microservices.notificationservice.controller;

import com.microservices.notificationservice.dto.CreateNewsRequest;
import com.microservices.notificationservice.dto.NewsArticleDto;
import com.microservices.notificationservice.dto.UpdateNewsRequest;
import com.microservices.notificationservice.service.NewsArticleService;
import com.microservices.notificationservice.util.RoleUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsArticleService newsArticleService;

    @GetMapping
    public ResponseEntity<List<NewsArticleDto>> list(@AuthenticationPrincipal Jwt jwt) {
        requireAuthenticated(jwt);
        return ResponseEntity.ok(newsArticleService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsArticleDto> getOne(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        requireAuthenticated(jwt);
        return ResponseEntity.ok(newsArticleService.getById(id));
    }

    @PostMapping
    public ResponseEntity<NewsArticleDto> create(
            @RequestBody CreateNewsRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        requireAdmin(jwt);
        NewsArticleDto created = newsArticleService.create(request, jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NewsArticleDto> update(
            @PathVariable Long id,
            @RequestBody UpdateNewsRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        requireAdmin(jwt);
        return ResponseEntity.ok(newsArticleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        requireAdmin(jwt);
        newsArticleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static void requireAuthenticated(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    private static void requireAdmin(Jwt jwt) {
        requireAuthenticated(jwt);
        if (!RoleUtil.isAdmin(jwt)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }
    }
}
