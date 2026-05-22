package com.coolai.agent.controller;

import com.coolai.agent.engine.PlanExecuteAgent;
import com.coolai.agent.service.AgentService;
import com.coolai.common.model.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/agent")
@RequiredArgsConstructor
@Tag(name = "Agent", description = "AI Agent APIs")
public class AgentController {

    private final AgentService agentService;

    @PostMapping("/execute")
    @Operation(summary = "Execute agent task")
    public ApiResponse<PlanExecuteAgent.AgentResponse> execute(
            @RequestBody Map<String, String> request) {
        String userId = getCurrentUserId();
        String query = request.get("query");
        String model = request.getOrDefault("model", "mimo-v2.5-pro");
        return ApiResponse.ok(agentService.execute(userId, query, model));
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (String) authentication.getPrincipal();
    }
}
