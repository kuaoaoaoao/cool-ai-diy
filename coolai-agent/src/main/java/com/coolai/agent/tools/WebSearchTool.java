package com.coolai.agent.tools;

import com.coolai.common.mcp.McpTool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Web Search MCP Tool - Search the web using Tavily API
 */
@Slf4j
@Component
public class WebSearchTool implements McpTool {

    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    @Value("${coolai.mcp.tavily.api-key:}")
    private String apiKey;

    public WebSearchTool(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder().build();
    }

    @Override
    public String name() {
        return "web_search";
    }

    @Override
    public String description() {
        return "Search the web for information using Tavily API. Input: {\"query\": \"search text\"}";
    }

    @Override
    public JsonNode parameters() {
        ObjectNode params = JsonNodeFactory.instance.objectNode();
        params.put("type", "object");
        ObjectNode properties = params.putObject("properties");
        properties.putObject("query").put("type", "string").put("description", "Search query");
        params.putArray("required").add("query");
        return params;
    }

    @Override
    public CompletableFuture<JsonNode> execute(JsonNode arguments) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String query = arguments.path("query").asText();
                log.info("Web search: query={}", query);

                if (apiKey == null || apiKey.isEmpty()) {
                    ObjectNode result = JsonNodeFactory.instance.objectNode();
                    result.put("status", "error");
                    result.put("message", "Tavily API key not configured");
                    return result;
                }

                String jsonBody = objectMapper.writeValueAsString(java.util.Map.of(
                        "api_key", apiKey,
                        "query", query,
                        "max_results", 5
                ));

                Request request = new Request.Builder()
                        .url("https://api.tavily.com/search")
                        .addHeader("Content-Type", "application/json")
                        .post(okhttp3.RequestBody.create(jsonBody,
                                okhttp3.MediaType.parse("application/json")))
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        return objectMapper.readTree(response.body().string());
                    } else {
                        ObjectNode error = JsonNodeFactory.instance.objectNode();
                        error.put("error", "Tavily API error: " + response.code());
                        return error;
                    }
                }
            } catch (Exception e) {
                log.error("Web search failed", e);
                ObjectNode error = JsonNodeFactory.instance.objectNode();
                error.put("error", e.getMessage());
                return error;
            }
        });
    }
}
