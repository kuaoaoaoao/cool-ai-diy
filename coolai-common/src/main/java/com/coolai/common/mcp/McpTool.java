package com.coolai.common.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * MCP Tool interface
 */
public interface McpTool {

    String name();

    String description();

    JsonNode parameters();

    CompletableFuture<JsonNode> execute(JsonNode arguments);

    /**
     * Convert to OpenAI Function format
     */
    default Map<String, Object> toOpenAiFunction() {
        return Map.of(
                "name", name(),
                "description", description(),
                "parameters", parameters()
        );
    }
}
