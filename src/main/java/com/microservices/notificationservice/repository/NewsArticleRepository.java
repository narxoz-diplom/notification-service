package com.microservices.notificationservice.repository;

import com.microservices.notificationservice.model.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    List<NewsArticle> findAllByOrderByPublishedAtDesc();
}
