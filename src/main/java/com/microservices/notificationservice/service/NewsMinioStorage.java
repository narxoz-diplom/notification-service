package com.microservices.notificationservice.service;

import com.microservices.notificationservice.config.MinioConfig;
import io.minio.*;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsMinioStorage {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @PostConstruct
    public void initBucket() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .build());
                log.info("MinIO bucket '{}' created for news storage", minioConfig.getBucketName());
            }
        } catch (Exception e) {
            log.error("Failed to ensure MinIO bucket exists", e);
        }
    }

    public String uploadTextContent(String text) throws IOException, ServerException, InsufficientDataException,
            ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException,
            XmlParserException, InternalException {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        String objectName = "news/" + UUID.randomUUID() + "_content.txt";
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(objectName)
                .stream(new ByteArrayInputStream(data), data.length, -1)
                .contentType("text/plain; charset=utf-8")
                .build());
        return objectName;
    }

    public String readTextContent(String objectName) throws IOException {
        try (InputStream in = minioClient.getObject(GetObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(objectName)
                .build())) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to read news content from MinIO: {}", objectName, e);
            throw new IOException("Could not read news content", e);
        }
    }

    public void deleteObject(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return;
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to delete MinIO object {}: {}", objectName, e.getMessage());
        }
    }
}
