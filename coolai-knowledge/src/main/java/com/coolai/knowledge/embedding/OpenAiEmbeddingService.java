package com.coolai.knowledge.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * OpenAI Embedding Service - Use OpenAI text-embedding API
 */
@Slf4j
@Component
public class OpenAiEmbeddingService implements EmbeddingService {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${coolai.llm.openai.api-key:}")
    private String apiKey;

    @Value("${coolai.llm.openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    public OpenAiEmbeddingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public String getName() {
        return "openai";
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.startsWith("fake-");
    }

    @Override
    public List<float[]> embed(List<String> texts) {
        try {
            String json = objectMapper.writeValueAsString(java.util.Map.of(
                    "input", texts,
                    "model", "text-embedding-3-small"
            ));
            Request request = new Request.Builder()
                    .url(baseUrl + "/embeddings")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(okhttp3.RequestBody.create(json,
                            okhttp3.MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Embedding API error: " + response.code());
                }
                String body = response.body().string();
                JsonNode root = objectMapper.readTree(body);
                JsonNode data = root.path("data");

                List<float[]> embeddings = new ArrayList<>();
                for (JsonNode item : data) {
                    JsonNode embedding = item.path("embedding");
                    float[] vector = new float[embedding.size()];
                    for (int i = 0; i < embedding.size(); i++) {
                        vector[i] = (float) embedding.get(i).asDouble();
                    }
                    embeddings.add(vector);
                }
                return embeddings;
            }
        } catch (Exception e) {
            log.error("OpenAI embedding failed", e);
            throw new RuntimeException("Embedding failed: " + e.getMessage(), e);
        }
    }

    @Override
    public int getDimension() {
        return 1536;
    }
}
