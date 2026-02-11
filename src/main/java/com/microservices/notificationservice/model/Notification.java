package com.microservices.notificationservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    @Column(length = 1000)
    private String message;

    private String type;

    private Boolean read = false;

    private LocalDateTime createdAt;

    @PrePersist
    void createdAt() {
        this.createdAt = LocalDateTime.now();
    }
}
