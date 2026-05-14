package com.coolai.agent.engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanStep {

    private String stepId;
    private String description;
    private String toolName;
    private List<String> dependsOn;
    private String status; // pending, running, completed, failed
    private Object result;
}
