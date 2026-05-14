package com.coolai.agent.engine;

import com.coolai.common.llm.LlmGatewayService;
import com.coolai.common.llm.LlmProvider;
import com.coolai.common.mcp.McpRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Plan-Execute Agent Engine
 * Three phases: Plan → Execute → Summarize
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlanExecuteAgent {

    private final LlmGatewayService llmGatewayService;
    private final McpRegistry mcpRegistry;
    private final ObjectMapper objectMapper;

    public AgentResponse execute(String userId, String query, String model) {
        // Phase 1: Plan
        ExecutionPlan plan = planPhase(query, model);
        log.info("Plan created with {} steps for query: {}", plan.getSteps().size(), query);

        // Phase 2: Execute
        executePhase(plan);
        log.info("Plan execution completed. All steps: {}", plan.isAllCompleted());

        // Phase 3: Summarize
        String summary = summarizePhase(query, plan, model);
        log.info("Summary generated for query: {}", query);

        return AgentResponse.builder()
                .answer(summary)
                .plan(plan.getSteps())
                .stepResults(plan.getSteps().stream()
                        .collect(Collectors.toMap(PlanStep::getStepId, PlanStep::getResult)))
                .build();
    }

    private ExecutionPlan planPhase(String query, String model) {
        String planPrompt = buildPlanPrompt(query);
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", planPrompt),
                Map.of("role", "user", "content", query)
        );

        LlmProvider.LlmRequest request = new LlmProvider.LlmRequest(
                model != null ? model : "gpt-3.5-turbo",
                messages,
                0.3f,
                1024,
                null
        );

        LlmProvider.LlmResponse response = llmGatewayService.chat(request);
        return parsePlan(query, response.content());
    }

    private void executePhase(ExecutionPlan plan) {
        while (!plan.isAllCompleted()) {
            List<PlanStep> readySteps = plan.getReadySteps();
            if (readySteps.isEmpty()) {
                break;
            }

            for (PlanStep step : readySteps) {
                step.setStatus("running");
                try {
                    Object result = executeStep(step);
                    step.setResult(result);
                    step.setStatus("completed");
                } catch (Exception e) {
                    log.error("Step {} failed: {}", step.getStepId(), e.getMessage());
                    step.setResult(Map.of("error", e.getMessage()));
                    step.setStatus("failed");
                }
            }
        }
    }

    private Object executeStep(PlanStep step) {
        if (step.getToolName() == null || step.getToolName().isEmpty()) {
            return Map.of("message", "No tool specified, step completed");
        }

        try {
            JsonNode args = objectMapper.valueToTree(
                    Map.of("query", step.getDescription()));
            return mcpRegistry.executeTool(step.getToolName(), args).join();
        } catch (Exception e) {
            return Map.of("error", "Tool execution failed: " + e.getMessage());
        }
    }

    private String summarizePhase(String query, ExecutionPlan plan, String model) {
        StringBuilder context = new StringBuilder();
        context.append("Original query: ").append(query).append("\n\n");
        context.append("Execution results:\n");

        for (PlanStep step : plan.getSteps()) {
            context.append("- ").append(step.getDescription())
                    .append(": ").append(step.getResult()).append("\n");
        }

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content",
                        "Based on the execution results, provide a comprehensive answer to the user's question."),
                Map.of("role", "user", "content", context.toString())
        );

        LlmProvider.LlmRequest request = new LlmProvider.LlmRequest(
                model != null ? model : "gpt-3.5-turbo",
                messages,
                0.7f,
                2048,
                null
        );

        LlmProvider.LlmResponse response = llmGatewayService.chat(request);
        return response.content();
    }

    private String buildPlanPrompt(String query) {
        List<Map<String, Object>> availableTools = mcpRegistry.toOpenAiFunctions();
        String toolDescriptions = availableTools.stream()
                .map(t -> "- " + t.get("name") + ": " + t.get("description"))
                .collect(Collectors.joining("\n"));

        return """
                You are a planning agent. Break down the user's request into executable steps.
                
                Available tools:
                %s
                
                Respond with a JSON array of steps. Each step should have:
                - stepId: unique identifier
                - description: what to do
                - toolName: the tool to use (or null if no tool needed)
                - dependsOn: list of stepIds this step depends on (or empty array)
                
                Example response:
                [{"stepId":"1","description":"Search for information","toolName":"web_search","dependsOn":[]}]
                """.formatted(toolDescriptions);
    }

    private ExecutionPlan parsePlan(String query, String planContent) {
        try {
            String json = planContent;
            if (planContent.contains("```")) {
                json = planContent.substring(planContent.indexOf("["));
                json = json.substring(0, json.lastIndexOf("]") + 1);
            }

            PlanStep[] steps = objectMapper.readValue(json, PlanStep[].class);
            return ExecutionPlan.builder()
                    .objective(query)
                    .steps(Arrays.asList(steps))
                    .build();
        } catch (Exception e) {
            log.warn("Failed to parse plan, creating single-step plan: {}", e.getMessage());
            return ExecutionPlan.builder()
                    .objective(query)
                    .steps(List.of(
                            PlanStep.builder()
                                    .stepId("1")
                                    .description(query)
                                    .toolName(null)
                                    .dependsOn(List.of())
                                    .status("pending")
                                    .build()
                    ))
                    .build();
        }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AgentResponse {
        private String answer;
        private List<PlanStep> plan;
        private Map<String, Object> stepResults;
    }
}
