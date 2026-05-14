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
 * Get Weather MCP Tool - Get weather information from OpenWeatherMap
 */
@Slf4j
@Component
public class GetWeatherTool implements McpTool {

    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    @Value("${coolai.mcp.openweathermap.api-key:}")
    private String apiKey;

    public GetWeatherTool(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder().build();
    }

    @Override
    public String name() {
        return "get_weather";
    }

    @Override
    public String description() {
        return "Get current weather for a location. Input: {\"location\": \"city name\"}";
    }

    @Override
    public JsonNode parameters() {
        ObjectNode params = JsonNodeFactory.instance.objectNode();
        params.put("type", "object");
        ObjectNode properties = params.putObject("properties");
        properties.putObject("location").put("type", "string").put("description", "City name or location");
        params.putArray("required").add("location");
        return params;
    }

    @Override
    public CompletableFuture<JsonNode> execute(JsonNode arguments) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String location = arguments.path("location").asText();
                log.info("Weather request: location={}", location);

                if (apiKey == null || apiKey.isEmpty()) {
                    ObjectNode result = JsonNodeFactory.instance.objectNode();
                    result.put("status", "error");
                    result.put("message", "OpenWeatherMap API key not configured");
                    return result;
                }

                String url = String.format(
                        "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric",
                        java.net.URLEncoder.encode(location, "UTF-8"), apiKey);

                Request request = new Request.Builder().url(url).build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        return objectMapper.readTree(response.body().string());
                    } else {
                        ObjectNode error = JsonNodeFactory.instance.objectNode();
                        error.put("error", "Weather API error: " + response.code());
                        return error;
                    }
                }
            } catch (Exception e) {
                log.error("Weather tool failed", e);
                ObjectNode error = JsonNodeFactory.instance.objectNode();
                error.put("error", e.getMessage());
                return error;
            }
        });
    }
}
