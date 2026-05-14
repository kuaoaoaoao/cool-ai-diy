package com.coolai.common.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP Tool Registry - Register and manage MCP tools
 */
@Slf4j
@Service
public class McpRegistry {

    private final Map<String, McpTool> tools = new ConcurrentHashMap<>();

    public void registerTool(McpTool tool) {
        tools.put(tool.name(), tool);
        log.info("Registered MCP tool: {}", tool.name());
    }

    public void unregisterTool(String name) {
        tools.remove(name);
    }

    public List<McpTool> discoverTools() {
        return new ArrayList<>(tools.values());
    }

    public CompletableFuture<JsonNode> executeTool(String name, JsonNode arguments) {
        McpTool tool = tools.get(name);
        if (tool == null) {
            return CompletableFuture.failedFuture(
                    new RuntimeException("Tool not found: " + name));
        }
        return tool.execute(arguments);
    }

    public List<Map<String, Object>> toOpenAiFunctions() {
        List<Map<String, Object>> functions = new ArrayList<>();
        for (McpTool tool : tools.values()) {
            functions.add(tool.toOpenAiFunction());
        }
        return functions;
    }

    public Optional<McpTool> getTool(String name) {
        return Optional.ofNullable(tools.get(name));
    }
}
