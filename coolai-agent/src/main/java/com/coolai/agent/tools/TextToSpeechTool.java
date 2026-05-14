package com.coolai.agent.tools;

import com.coolai.common.mcp.McpTool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Text-to-Speech MCP Tool - Convert text to speech audio
 */
@Slf4j
@Component
public class TextToSpeechTool implements McpTool {

    @Override
    public String name() {
        return "text_to_speech";
    }

    @Override
    public String description() {
        return "Convert text to speech audio. Input: {\"text\": \"text to speak\", \"voice\": \"voice id\"}";
    }

    @Override
    public JsonNode parameters() {
        ObjectNode params = JsonNodeFactory.instance.objectNode();
        params.put("type", "object");
        ObjectNode properties = params.putObject("properties");
        properties.putObject("text").put("type", "string").put("description", "Text to convert to speech");
        properties.putObject("voice").put("type", "string").put("description", "Voice ID to use");
        params.putArray("required").add("text");
        return params;
    }

    @Override
    public CompletableFuture<JsonNode> execute(JsonNode arguments) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String text = arguments.path("text").asText();
                String voice = arguments.path("voice").asText("zh-CN-XiaoxiaoNeural");
                log.info("TTS tool: textLength={}, voice={}", text.length(), voice);

                // In production, would call TtsService
                ObjectNode result = JsonNodeFactory.instance.objectNode();
                result.put("status", "success");
                result.put("message", "TTS synthesis would be triggered here");
                result.put("text_length", text.length());
                result.put("voice", voice);
                return result;
            } catch (Exception e) {
                log.error("TTS tool failed", e);
                ObjectNode error = JsonNodeFactory.instance.objectNode();
                error.put("error", e.getMessage());
                return error;
            }
        });
    }
}
