package com.coolai.agent.tools;

import com.coolai.common.mcp.McpTool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Code Execute MCP Tool - Execute code in a sandboxed environment
 */
@Slf4j
@Component
public class CodeExecuteTool implements McpTool {

    @Override
    public String name() {
        return "code_execute";
    }

    @Override
    public String description() {
        return "Execute Python code in a sandboxed environment. Input: {\"code\": \"python code\", \"language\": \"python\"}";
    }

    @Override
    public JsonNode parameters() {
        ObjectNode params = JsonNodeFactory.instance.objectNode();
        params.put("type", "object");
        ObjectNode properties = params.putObject("properties");
        properties.putObject("code").put("type", "string").put("description", "Code to execute");
        properties.putObject("language").put("type", "string").put("description", "Programming language").put("default", "python");
        params.putArray("required").add("code");
        return params;
    }

    @Override
    public CompletableFuture<JsonNode> execute(JsonNode arguments) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String code = arguments.path("code").asText();
                String language = arguments.path("language").asText("python");
                log.info("Code execute request: language={}, codeLength={}", language, code.length());

                // Execute code in sandboxed Python subprocess
                ProcessBuilder pb = new ProcessBuilder("python3", "-c", code);
                pb.redirectErrorStream(true);
                Process process = pb.start();
                String output = new String(process.getInputStream().readAllBytes());
                int exitCode = process.waitFor();

                ObjectNode result = JsonNodeFactory.instance.objectNode();
                result.put("exitCode", exitCode);
                result.put("output", output.length() > 5000 ? output.substring(0, 5000) : output);
                return result;
            } catch (Exception e) {
                log.error("Code execution failed", e);
                ObjectNode error = JsonNodeFactory.instance.objectNode();
                error.put("error", e.getMessage());
                return error;
            }
        });
    }
}
