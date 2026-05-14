package com.coolai.agent.engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionPlan {

    private List<PlanStep> steps;
    private String objective;

    public List<PlanStep> getReadySteps() {
        List<String> completedStepIds = steps.stream()
                .filter(s -> "completed".equals(s.getStatus()))
                .map(PlanStep::getStepId)
                .collect(Collectors.toList());

        return steps.stream()
                .filter(s -> "pending".equals(s.getStatus()))
                .filter(s -> {
                    if (s.getDependsOn() == null || s.getDependsOn().isEmpty()) {
                        return true;
                    }
                    return completedStepIds.containsAll(s.getDependsOn());
                })
                .collect(Collectors.toList());
    }

    public boolean isAllCompleted() {
        return steps.stream().allMatch(s -> "completed".equals(s.getStatus()) || "failed".equals(s.getStatus()));
    }

    public void addStep(PlanStep step) {
        if (this.steps == null) {
            this.steps = new ArrayList<>();
        }
        this.steps.add(step);
    }
}
