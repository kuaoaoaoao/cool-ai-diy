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

/**
 * Anthropic Claude API Provider
 */
@Slf4j
@Component
public class AnthropicProvider implements LlmProvider {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${coolai.llm.anthropic.api-key:}")
    private String apiKey;

    @Value("${coolai.llm.anthropic.base-url:https://api.anthropic.com/v1}")
    private String baseUrl;

    @Value("${coolai.llm.anthropic.models:claude-3-haiku-20240307,claude-3-sonnet-20240229}")
    private String models;

    public AnthropicProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public String getName() {
        return "anthropic";
    }

    @Override
    public LlmResponse chat(LlmRequest request) {
        try {
            // Convert OpenAI-style messages to Anthropic format
            Map<String, Object> body = new HashMap<>();
            body.put("model", request.model());
            body.put("max_tokens", request.maxTokens() > 0 ? request.maxTokens() : 2048);

            // Anthropic uses separate system prompt
            List<Map<String, String>> messages = new ArrayList<>();
            String systemPrompt = null;
            for (Map<String, String> msg : request.messages()) {
                if ("system".equals(msg.get("role"))) {
                    systemPrompt = msg.get("content");
                } else {
                    messages.add(Map.of(
                            "role", msg.get("role"),
                            "content", msg.get("content")
                    ));
                }
            }
            body.put("messages", messages);
            if (systemPrompt != null) {
                body.put("system", systemPrompt);
            }
            if (request.temperature() > 0) {
                body.put("temperature", request.temperature());
            }

            String json = objectMapper.writeValueAsString(body);
            Request httpRequest = new Request.Builder()
                    .url(baseUrl + "/messages")
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", "2023-06-01")
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(json, MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Anthropic API error: " + response.code());
                }
                String responseBody = response.body().string();
                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode content = root.path("content").path(0);

                return new LlmResponse(
                        content.path("text").asText(),
                        root.path("model").asText(),
                        Map.of(
                                "inputTokens", root.path("usage").path("input_tokens").asInt(),
                                "outputTokens", root.path("usage").path("output_tokens").asInt()
                        ),
                        root.path("stop_reason").asText("end_turn")
                );
            }
        } catch (IOException e) {
            log.error("Anthropic chat error", e);
            throw new RuntimeException("Failed to call Anthropic API", e);
        }
    }

    @Override
    public void chatStream(LlmRequest request, StreamCallback callback) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", request.model());
            body.put("max_tokens", request.maxTokens() > 0 ? request.maxTokens() : 2048);
            body.put("stream", true);

            List<Map<String, String>> messages = new ArrayList<>();
            String systemPrompt = null;
            for (Map<String, String> msg : request.messages()) {
                if ("system".equals(msg.get("role"))) {
                    systemPrompt = msg.get("content");
                } else {
                    messages.add(Map.of("role", msg.get("role"), "content", msg.get("content")));
                }
            }
            body.put("messages", messages);
            if (systemPrompt != null) {
                body.put("system", systemPrompt);
            }

            String json = objectMapper.writeValueAsString(body);
            Request httpRequest = new Request.Builder()
                    .url(baseUrl + "/messages")
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", "2023-06-01")
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
                        java.io.BufferedReader reader = new java.io.BufferedReader(
                                new java.io.InputStreamReader(responseBody.byteStream()));
                        StringBuilder fullContent = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6).trim();
                                if ("[DONE]".equals(data)) break;
                                JsonNode chunk = objectMapper.readTree(data);
                                String type = chunk.path("type").asText();
                                if ("content_block_delta".equals(type)) {
                                    String text = chunk.path("delta").path("text").asText("");
                                    if (!text.isEmpty()) {
                                        fullContent.append(text);
                                        callback.onToken(text);
                                    }
                                }
                            }
                        }
                        callback.onDone(new LlmResponse(
                                fullContent.toString(), request.model(),
                                Map.of("estimatedTokens", fullContent.length() / 4), "end_turn"));
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
        // Anthropic doesn't provide embedding API; delegate to OpenAI
        throw new UnsupportedOperationException("Anthropic does not provide embedding API. Use OpenAI instead.");
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.startsWith("fake-");
    }

    @Override
    public List<String> listModels() {
        return Arrays.asList(models.split(","));
    }
}
