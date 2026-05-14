package com.coolai.common.llm;

import java.util.List;
import java.util.Map;

/**
 * LLM Provider interface - Strategy pattern for multiple LLM backends
 */
public interface LlmProvider {

    /**
     * Get provider name
     */
    String getName();

    /**
     * Synchronous chat completion
     */
    LlmResponse chat(LlmRequest request);

    /**
     * Stream chat completion (returns SSE event data)
     */
    void chatStream(LlmRequest request, StreamCallback callback);

    /**
     * Generate text embeddings
     */
    List<float[]> embeddings(List<String> texts);

    /**
     * Check if the provider is available
     */
    boolean isAvailable();

    /**
     * Get available models
     */
    List<String> listModels();

    /**
     * LLM Request
     */
    record LlmRequest(
            String model,
            List<Map<String, String>> messages,
            float temperature,
            int maxTokens,
            List<Map<String, Object>> tools
    ) {}

    /**
     * LLM Response
     */
    record LlmResponse(
            String content,
            String model,
            Map<String, Object> usage,
            String finishReason
    ) {}

    /**
     * Stream callback interface
     */
    interface StreamCallback {
        void onToken(String token);
        void onDone(LlmResponse response);
        void onError(Exception e);
    }
}
