package com.coolai.common.llm;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LLM Gateway Service - Model registration, routing, and failover
 * 自动注入所有 LlmProvider 实现，MockLlmProvider 作为最终回退
 */
@Slf4j
@Service
public class LlmGatewayService {

    private final Map<String, LlmProvider> providers = new ConcurrentHashMap<>();
    private final Map<String, String> modelToProvider = new ConcurrentHashMap<>();

    @Autowired
    private List<LlmProvider> injectedProviders;

    @Autowired
    private MockLlmProvider mockLlmProvider;

    @PostConstruct
    public void init() {
        // 注册所有注入的 Provider
        for (LlmProvider provider : injectedProviders) {
            if (!(provider instanceof MockLlmProvider)) {
                registerProvider(provider);
            }
        }
        // MockLlmProvider 始终最后注册，作为最终回退
        registerProvider(mockLlmProvider);

        long realProviderCount = injectedProviders.stream()
                .filter(p -> !(p instanceof MockLlmProvider) && p.isAvailable())
                .count();
        if (realProviderCount == 0) {
            log.warn("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.warn("⚠️  未检测到真实 LLM Provider，已启用 Mock 模式");
            log.warn("   所有对话将返回模拟回复，不消耗 API 额度");
            log.warn("   要启用真实 AI 对话，请设置环境变量：");
            log.warn("   MIMO_API_KEY=sk-xxx 或 OPENAI_API_KEY=sk-xxx");
            log.warn("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        } else {
            log.info("已注册 {} 个真实 LLM Provider + 1 个 Mock 回退", realProviderCount);
        }
    }

    public void registerProvider(LlmProvider provider) {
        providers.put(provider.getName(), provider);
        for (String model : provider.listModels()) {
            modelToProvider.put(model, provider.getName());
        }
        log.info("Registered LLM provider: {} with models: {}", provider.getName(), provider.listModels());
    }

    public LlmProvider getProvider(String modelName) {
        // 优先匹配指定模型的 Provider
        String providerName = modelToProvider.get(modelName);
        if (providerName != null) {
            LlmProvider provider = providers.get(providerName);
            if (provider != null && provider.isAvailable()) {
                return provider;
            }
        }
        // 回退：优先使用真实 Provider，最后使用 Mock
        return findAvailableProvider();
    }

    public LlmProvider.LlmResponse chat(LlmProvider.LlmRequest request) {
        LlmProvider provider = getProvider(request.model());
        return provider.chat(request);
    }

    public void chatStream(LlmProvider.LlmRequest request, LlmProvider.StreamCallback callback) {
        LlmProvider provider = getProvider(request.model());
        provider.chatStream(request, callback);
    }

    public List<float[]> embeddings(List<String> texts) {
        // 优先使用真实嵌入 Provider，回退到 Mock
        LlmProvider provider = providers.values().stream()
                .filter(p -> p.isAvailable() && !(p instanceof MockLlmProvider))
                .findFirst()
                .orElse(mockLlmProvider);
        return provider.embeddings(texts);
    }

    public List<Map<String, Object>> listModels() {
        List<Map<String, Object>> modelList = new ArrayList<>();
        for (Map.Entry<String, LlmProvider> entry : providers.entrySet()) {
            Map<String, Object> providerInfo = new HashMap<>();
            providerInfo.put("provider", entry.getKey());
            providerInfo.put("available", entry.getValue().isAvailable());
            providerInfo.put("models", entry.getValue().listModels());
            modelList.add(providerInfo);
        }
        return modelList;
    }

    /**
     * 查找可用 Provider：优先真实 Provider，Mock 始终作为最终回退
     */
    private LlmProvider findAvailableProvider() {
        // 优先非 Mock 的可用 Provider
        Optional<LlmProvider> realProvider = providers.values().stream()
                .filter(p -> p.isAvailable() && !(p instanceof MockLlmProvider))
                .findFirst();
        if (realProvider.isPresent()) {
            return realProvider.get();
        }
        // Mock Provider 始终可用
        if (mockLlmProvider.isAvailable()) {
            log.debug("Using MockLlmProvider as fallback");
            return mockLlmProvider;
        }
        throw new RuntimeException("No available LLM provider (including mock)");
    }
}
