package com.coolai.agent.service;

import com.coolai.agent.engine.PlanExecuteAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final PlanExecuteAgent planExecuteAgent;

    public PlanExecuteAgent.AgentResponse execute(String userId, String query, String model) {
        return planExecuteAgent.execute(userId, query, model);
    }
}
