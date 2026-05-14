package com.coolai.admin.service;

import com.coolai.common.llm.LlmGatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemService {

    private final LlmGatewayService llmGatewayService;

    public Map<String, Object> getSystemStats() {
        return Map.of(
                "totalUsers", 0,
                "totalConversations", 0,
                "totalMessages", 0,
                "totalTtsTasks", 0,
                "totalVideoTasks", 0,
                "totalSingingTasks", 0,
                "totalReplaceTasks", 0,
                "uptime", System.currentTimeMillis(),
                "llmProviders", llmGatewayService.listModels()
        );
    }

    public List<Map<String, Object>> getAvailableModels() {
        return llmGatewayService.listModels();
    }

    public Map<String, Object> getSystemConfig() {
        return Map.of(
                "version", "2.0.0",
                "javaVersion", System.getProperty("java.version"),
                "springBootVersion", "3.2.5"
        );
    }
}
