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
 * Remote MCP Server - Connect to remote MCP servers via HTTP/SSE + JSON-RPC 2.0
 */
@Slf4j
@Component
public class RemoteMcpServer implements McpTool {

    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    @Value("${coolai.mcp.remote.url:}")
    private String remoteUrl;

    public RemoteMcpServer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder().build();
    }

    @Override
    public String name() {
        return "remote_mcp";
    }

    @Override
    public String description() {
        return "Call a remote MCP server tool via JSON-RPC 2.0. Input: {\"method\": \"tool_name\", \"params\": {}}";
    }

    @Override
    public JsonNode parameters() {
        ObjectNode params = JsonNodeFactory.instance.objectNode();
        params.put("type", "object");
        ObjectNode properties = params.putObject("properties");
        properties.putObject("method").put("type", "string").put("description", "Remote tool method name");
        properties.putObject("params").put("type", "object").put("description", "Parameters for the remote tool");
        params.putArray("required").add("method");
        return params;
    }

    @Override
    public CompletableFuture<JsonNode> execute(JsonNode arguments) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String method = arguments.path("method").asText();
                JsonNode params = arguments.path("params");

                if (remoteUrl == null || remoteUrl.isEmpty()) {
                    ObjectNode result = JsonNodeFactory.instance.objectNode();
                    result.put("status", "error");
                    result.put("message", "Remote MCP server URL not configured");
                    return result;
                }

                // Build JSON-RPC 2.0 request
                ObjectNode rpcRequest = JsonNodeFactory.instance.objectNode();
                rpcRequest.put("jsonrpc", "2.0");
                rpcRequest.put("method", method);
                rpcRequest.put("id", System.currentTimeMillis());
                if (!params.isMissingNode() && !params.isNull()) {
                    rpcRequest.set("params", params);
                }

                String jsonBody = objectMapper.writeValueAsString(rpcRequest);
                Request httpRequest = new Request.Builder()
                        .url(remoteUrl)
                        .addHeader("Content-Type", "application/json")
                        .post(okhttp3.RequestBody.create(jsonBody,
                                okhttp3.MediaType.parse("application/json")))
                        .build();

                try (Response response = httpClient.newCall(httpRequest).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonNode rpcResponse = objectMapper.readTree(response.body().string());
                        if (rpcResponse.has("result")) {
                            return rpcResponse.get("result");
                        } else if (rpcResponse.has("error")) {
                            ObjectNode error = JsonNodeFactory.instance.objectNode();
                            error.put("error", rpcResponse.get("error").path("message").asText("Unknown error"));
                            return error;
                        }
                        return rpcResponse;
                    } else {
                        ObjectNode error = JsonNodeFactory.instance.objectNode();
                        error.put("error", "Remote MCP error: " + response.code());
                        return error;
                    }
                }
            } catch (Exception e) {
                log.error("Remote MCP call failed", e);
                ObjectNode error = JsonNodeFactory.instance.objectNode();
                error.put("error", e.getMessage());
                return error;
            }
        });
    }
}
