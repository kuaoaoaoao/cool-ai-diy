package com.coolai.common.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "coolai.minio")
public class MinioConfig {

    private String endpoint = "http://localhost:9000";
    private String accessKey = "coolai";
    private String secretKey = "coolai12345";
    private String bucket = "coolai";
    private String region = "us-east-1";

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .region(region)
                .build();
    }
}
