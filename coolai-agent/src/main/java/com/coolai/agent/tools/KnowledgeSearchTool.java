package com.coolai.agent.tools;

import com.coolai.common.mcp.McpTool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Knowledge Search MCP Tool - Search knowledge base for relevant information
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeSearchTool implements McpTool {

    private final ObjectMapper objectMapper;

    @Override
    public String name() {
        return "knowledge_search";
    }

    @Override
    public String description() {
        return "Search the knowledge base for relevant information. Input: {\"query\": \"search text\", \"knowledge_base_id\": \"optional kb id\"}";
    }

    @Override
    public JsonNode parameters() {
        ObjectNode params = JsonNodeFactory.instance.objectNode();
        params.put("type", "object");
        ObjectNode properties = params.putObject("properties");
        properties.putObject("query").put("type", "string").put("description", "Search query text");
        properties.putObject("knowledge_base_id").put("type", "string").put("description", "Optional knowledge base ID to search in");
        params.putArray("required").add("query");
        return params;
    }

    @Override
    public CompletableFuture<JsonNode> execute(JsonNode arguments) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String query = arguments.path("query").asText();
                String kbId = arguments.path("knowledge_base_id").asText("");
                log.info("Knowledge search: query={}, kbId={}", query, kbId);

                // In production, would call RagPipeline or VectorStore
                ObjectNode result = JsonNodeFactory.instance.objectNode();
                result.put("status", "success");
                result.put("query", query);
                result.put("results", "Knowledge search results would be returned here");
                return result;
            } catch (Exception e) {
                log.error("Knowledge search failed", e);
                ObjectNode error = JsonNodeFactory.instance.objectNode();
                error.put("error", e.getMessage());
                return error;
            }
        });
    }
}
