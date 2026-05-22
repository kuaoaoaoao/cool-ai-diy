package com.coolai.common.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * MinIO Object Storage Service - Shared file storage for all modules
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(name = "minioClient")
public class MinioStorageService {

    private final MinioClient minioClient;

    @Value("${coolai.minio.bucket:coolai}")
    private String bucket;

    /**
     * Ensure bucket exists
     */
    public void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucket).build());
                log.info("Created MinIO bucket: {}", bucket);
            }
        } catch (Exception e) {
            log.error("Failed to ensure bucket exists", e);
        }
    }

    /**
     * Upload file
     */
    public void upload(String objectName, InputStream inputStream, String contentType, long size) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());
            log.debug("Uploaded file: {}", objectName);
        } catch (Exception e) {
            log.error("Failed to upload file: {}", objectName, e);
            throw new RuntimeException("File upload failed", e);
        }
    }

    /**
     * Get download URL (presigned)
     */
    public String getPresignedUrl(String objectName, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(expiryMinutes, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for: {}", objectName, e);
            throw new RuntimeException("Failed to generate download URL", e);
        }
    }

    /**
     * Download file as InputStream
     */
    public InputStream download(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("Failed to download file: {}", objectName, e);
            throw new RuntimeException("File download failed", e);
        }
    }

    /**
     * Delete file
     */
    public void delete(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            log.debug("Deleted file: {}", objectName);
        } catch (Exception e) {
            log.error("Failed to delete file: {}", objectName, e);
        }
    }

    /**
     * Check if file exists
     */
    public boolean exists(String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
