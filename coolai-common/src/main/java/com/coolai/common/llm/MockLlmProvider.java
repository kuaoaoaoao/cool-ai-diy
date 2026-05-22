package com.coolai.common.llm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Mock LLM Provider - 无需API Key即可运行的模拟LLM
 * 用于开发测试和学习项目
 */
@Slf4j
@Component
public class MockLlmProvider implements LlmProvider {

    private static final String MOCK_RESPONSE = "你好！我是 CoolAI 助手（模拟模式）。\n\n" +
            "当前未接入真实 LLM API，这是模拟回复。\n" +
            "要启用真实 AI 对话，请在配置中设置 MIMO_API_KEY。\n\n" +
            "你可以通过以下方式体验项目功能：\n" +
            "1. 注册/登录 - 完整可用\n" +
            "2. 创建会话/发消息 - 使用模拟回复\n" +
            "3. TTS语音合成 - Edge-TTS免费可用\n" +
            "4. 知识库CRUD - 完整可用\n" +
            "5. 视频生成/人物替换 - Mock引擎返回示例";

    private static final String RAG_RESPONSE_TEMPLATE = "基于知识库内容，以下是相关回答：\n\n%s\n\n" +
            "---\n*以上为模拟RAG回答，接入LLM后将提供智能生成*";

    @Override
    public String getName() {
        return "mock";
    }

    @Override
    public boolean isAvailable() {
        return true; // 始终可用
    }

    @Override
    public LlmResponse chat(LlmRequest request) {
        log.info("MockLLM: 模拟对话响应");

        // 检查是否是 RAG 模式（messages 中包含知识库上下文）
        String userMessage = extractLastUserMessage(request.messages());
        String response = generateSmartResponse(userMessage);

        return new LlmResponse(
                response,
                "mock-llm",
                Map.of(
                        "promptTokens", estimateTokens(userMessage),
                        "completionTokens", estimateTokens(response),
                        "totalTokens", estimateTokens(userMessage) + estimateTokens(response),
                        "mockProvider", true
                ),
                "stop"
        );
    }

    @Override
    public void chatStream(LlmRequest request, StreamCallback callback) {
        log.info("MockLLM: 模拟流式对话响应");
        String response = generateSmartResponse(extractLastUserMessage(request.messages()));

        // 模拟流式输出，逐字返回
        new Thread(() -> {
            try {
                for (char c : response.toCharArray()) {
                    callback.onToken(String.valueOf(c));
                    Thread.sleep(20); // 模拟打字速度
                }
                callback.onDone(new LlmResponse(
                        response, "mock-llm",
                        Map.of(
                                "promptTokens", estimateTokens(response),
                                "completionTokens", estimateTokens(response),
                                "totalTokens", estimateTokens(response) * 2,
                                "mockProvider", true
                        ),
                        "stop"
                ));
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    @Override
    public List<float[]> embeddings(List<String> texts) {
        // 返回确定性伪向量（基于文本hash）
        Random random = new Random(42);
        return texts.stream().map(text -> {
            random.setSeed(text.hashCode());
            float[] vector = new float[384];
            float sum = 0;
            for (int i = 0; i < 384; i++) {
                vector[i] = (float) random.nextGaussian();
                sum += vector[i] * vector[i];
            }
            float norm = (float) Math.sqrt((double) sum);
            for (int i = 0; i < 384; i++) {
                vector[i] /= norm;
            }
            return vector;
        }).toList();
    }

    @Override
    public List<String> listModels() {
        return List.of("mock-llm", "mock-llm-free");
    }

    private String extractLastUserMessage(List<Map<String, String>> messages) {
        if (messages == null || messages.isEmpty()) return "";
        for (int i = messages.size() - 1; i >= 0; i--) {
            if ("user".equals(messages.get(i).get("role"))) {
                return messages.get(i).get("content");
            }
        }
        return messages.get(messages.size() - 1).get("content");
    }

    private String generateSmartResponse(String userMessage) {
        if (userMessage == null || userMessage.isEmpty()) {
            return MOCK_RESPONSE;
        }

        String lower = userMessage.toLowerCase();

        // 智能模拟：根据用户输入返回不同回复
        if (lower.contains("你好") || lower.contains("hello") || lower.contains("hi")) {
            return "你好！我是 CoolAI 模拟助手。当前为模拟模式，接入 LLM API 后将提供真实 AI 对话。";
        }
        if (lower.contains("tts") || lower.contains("语音") || lower.contains("朗读")) {
            return "TTS 语音合成功能已就绪！你可以通过 TTS 模块体验 Edge-TTS 免费语音合成，无需 API Key。";
        }
        if (lower.contains("知识库") || lower.contains("rag") || lower.contains("文档")) {
            return "RAG 知识库功能已就绪。你可以创建知识库、上传文档、进行知识检索。当前使用本地模拟嵌入，接入专用 Embedding 服务后将提供语义级智能检索。";
        }
        if (lower.contains("克隆") || lower.contains("声音") || lower.contains("voice")) {
            return "声音克隆功能已就绪。Edge-TTS 降级引擎始终可用，接入 GPU 后可启用 XTTS-v2 零样本克隆。";
        }
        if (lower.contains("唱歌") || lower.contains("singing")) {
            return "AI 唱歌功能已就绪。当前 Edge-TTS 降级引擎可用（朗读歌词），接入 GPU 后可启用 RVC 真实歌声合成。";
        }
        if (lower.contains("视频") || lower.contains("video")) {
            return "视频生成功能已就绪。当前 Mock 引擎返回示例，接入 CogVideo GPU 或 Sora API 后将提供真实视频生成。";
        }
        if (lower.contains("换脸") || lower.contains("替换") || lower.contains("deepfake")) {
            return "人物替换功能已就绪。当前 Mock 引擎返回示例，接入 FaceFusion GPU 后将提供真实换脸功能（含强制水印和合规审核）。";
        }
        if (lower.contains("agent") || lower.contains("工具") || lower.contains("agent")) {
            return "AI Agent 功能已就绪。支持 Plan-Execute 三阶段执行模式，内置 5 个 MCP 工具（知识库检索、网络搜索、代码执行、TTS、天气查询）。";
        }
        if (lower.contains("帮助") || lower.contains("help") || lower.contains("功能")) {
            return """
                    CoolAI-Java 智能业务平台 功能清单：

                    📌 完全可用（无需API Key）：
                    - 用户注册/登录/JWT认证
                    - 会话管理（创建/列表/删除）
                    - TTS语音合成（Edge-TTS免费）
                    - 知识库CRUD（创建/上传文档/检索）
                    - 声音克隆（Edge-TTS降级）
                    - AI唱歌（Edge-TTS降级）
                    - 视频生成（Mock示例）
                    - 人物替换（Mock示例）

                    🔑 需要API Key：
                    - AI智能对话 → 需要 MIMO_API_KEY
                    - MiMo TTS → 需要 MIMO_API_KEY
                    - 网络搜索 → 需要 TAVILY_API_KEY
                    - 天气查询 → 需要 OPENWEATHERMAP_API_KEY

                    🖥️ 需要GPU：
                    - XTTS-v2声音克隆
                    - RVC/DiffSinger唱歌
                    - CogVideo视频生成
                    - FaceFusion换脸
                    """;
        }

        // 默认回复
        return MOCK_RESPONSE + "\n\n你的问题是：「" + userMessage + "」\n接入 LLM 后将获得智能回复。";
    }

    private int estimateTokens(String text) {
        if (text == null) return 0;
        return text.length() / 4; // 粗略估计
    }
}
