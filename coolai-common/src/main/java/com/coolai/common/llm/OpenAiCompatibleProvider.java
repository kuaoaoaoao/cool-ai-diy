package com.coolai.common.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OpenAiCompatibleProvider implements LlmProvider {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${coolai.llm.openai.api-key:}")
    private String apiKey;

    @Value("${coolai.llm.openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${coolai.llm.openai.models:gpt-4,gpt-3.5-turbo}")
    private String models;

    public OpenAiCompatibleProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public String getName() {
        return "openai";
    }

    @Override
    public LlmResponse chat(LlmRequest request) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", request.model());
            body.put("messages", request.messages());
            body.put("temperature", request.temperature() > 0 ? request.temperature() : 0.7f);
            body.put("max_tokens", request.maxTokens() > 0 ? request.maxTokens() : 2048);
            if (request.tools() != null && !request.tools().isEmpty()) {
                body.put("tools", request.tools());
            }
            body.put("stream", false);

            String json = objectMapper.writeValueAsString(body);
            Request httpRequest = new Request.Builder()
                    .url(baseUrl + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(json, MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("OpenAI API error: " + response.code());
                }
                String responseBody = response.body().string();
                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode choice = root.path("choices").path(0);
                JsonNode message = choice.path("message");

                return new LlmResponse(
                        message.path("content").asText(),
                        root.path("model").asText(),
                        Map.of(
                                "promptTokens", root.path("usage").path("prompt_tokens").asInt(),
                                "completionTokens", root.path("usage").path("completion_tokens").asInt(),
                                "totalTokens", root.path("usage").path("total_tokens").asInt()
                        ),
                        choice.path("finish_reason").asText()
                );
            }
        } catch (IOException e) {
            log.error("OpenAI chat error", e);
            throw new RuntimeException("Failed to call OpenAI API", e);
        }
    }

    @Override
    public void chatStream(LlmRequest request, StreamCallback callback) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", request.model());
            body.put("messages", request.messages());
            body.put("temperature", request.temperature() > 0 ? request.temperature() : 0.7f);
            body.put("max_tokens", request.maxTokens() > 0 ? request.maxTokens() : 2048);
            body.put("stream", true);

            String json = objectMapper.writeValueAsString(body);
            Request httpRequest = new Request.Builder()
                    .url(baseUrl + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(json, MediaType.parse("application/json")))
                    .build();

            httpClient.newCall(httpRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful() || responseBody == null) {
                            callback.onError(new RuntimeException("API error: " + response.code()));
                            return;
                        }
                        // Process SSE stream
                        String line;
                        java.io.BufferedReader reader = new java.io.BufferedReader(
                                new java.io.InputStreamReader(responseBody.byteStream()));
                        StringBuilder fullContent = new StringBuilder();

                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6).trim();
                                if ("[DONE]".equals(data)) {
                                    break;
                                }
                                JsonNode chunk = objectMapper.readTree(data);
                                JsonNode delta = chunk.path("choices").path(0).path("delta");
                                String content = delta.path("content").asText("");
                                if (!content.isEmpty()) {
                                    fullContent.append(content);
                                    callback.onToken(content);
                                }
                            }
                        }

                        callback.onDone(new LlmResponse(
                                fullContent.toString(),
                                request.model(),
                                Map.of("estimatedTokens", fullContent.length() / 4),
                                "stop"
                        ));
                    } catch (Exception e) {
                        callback.onError(e);
                    }
                }
            });
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    @Override
    public List<float[]> embeddings(List<String> texts) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("input", texts);
            body.put("model", "text-embedding-3-small");

            String json = objectMapper.writeValueAsString(body);
            Request httpRequest = new Request.Builder()
                    .url(baseUrl + "/embeddings")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(json, MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("OpenAI Embedding API error: " + response.code());
                }
                String responseBody = response.body().string();
                JsonNode root = objectMapper.readTree(responseBody);
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
        } catch (IOException e) {
            log.error("OpenAI embeddings error", e);
            throw new RuntimeException("Failed to call OpenAI Embedding API", e);
        }
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isEmpty();
    }

    @Override
    public List<String> listModels() {
        return Arrays.asList(models.split(","));
    }
}
