package com.coolai.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coolai.chat.mapper.MessageMapper;
import com.coolai.common.llm.LlmGatewayService;
import com.coolai.common.llm.LlmProvider;
import com.coolai.common.model.PageResult;
import com.coolai.model.dto.ChatRequest;
import com.coolai.model.dto.ChatResponse;
import com.coolai.model.entity.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageMapper messageMapper;
    private final ConversationService conversationService;
    private final LlmGatewayService llmGatewayService;

    private final ExecutorService sseExecutor = Executors.newCachedThreadPool();

    public ChatResponse chat(String userId, ChatRequest request) {
        // Get or create conversation
        String conversationId = request.getConversationId();
        if (conversationId == null || conversationId.isEmpty()) {
            var conversation = conversationService.createConversation(
                    userId, null, request.getModel());
            conversationId = conversation.getId();
        }

        // Save user message
        Message userMsg = new Message();
        userMsg.setConvId(conversationId);
        userMsg.setRole("user");
        userMsg.setContent(request.getContent());
        userMsg.setMetadata(new HashMap<>());
        userMsg.setTokens(0);
        userMsg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(userMsg);

        // Load conversation history
        List<Message> history = loadRecentMessages(conversationId, 20);
        List<Map<String, String>> messages = new ArrayList<>();
        for (Message msg : history) {
            messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }

        // Call LLM
        LlmProvider.LlmRequest llmRequest = new LlmProvider.LlmRequest(
                request.getModel() != null ? request.getModel() : "mimo-v2.5-pro",
                messages,
                0.7f,
                2048,
                null
        );

        LlmProvider.LlmResponse llmResponse = llmGatewayService.chat(llmRequest);

        // Save assistant message
        Message assistantMsg = new Message();
        assistantMsg.setConvId(conversationId);
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(llmResponse.content());
        assistantMsg.setMetadata(new HashMap<>());
        assistantMsg.setTokens((int) llmResponse.usage().getOrDefault("completionTokens", 0));
        assistantMsg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(assistantMsg);

        return ChatResponse.builder()
                .conversationId(conversationId)
                .messageId(assistantMsg.getId())
                .content(llmResponse.content())
                .role("assistant")
                .usage(llmResponse.usage())
                .build();
    }

    public SseEmitter chatStream(String userId, ChatRequest request) {
        SseEmitter emitter = new SseEmitter(300_000L);

        sseExecutor.execute(() -> {
            try {
                // Get or create conversation
                String convId = request.getConversationId();
                if (convId == null || convId.isEmpty()) {
                    var conversation = conversationService.createConversation(
                            userId, null, request.getModel());
                    convId = conversation.getId();
                }
                final String conversationId = convId;

                // Save user message
                Message userMsg = new Message();
                userMsg.setConvId(conversationId);
                userMsg.setRole("user");
                userMsg.setContent(request.getContent());
                userMsg.setMetadata(new HashMap<>());
                userMsg.setTokens(0);
                userMsg.setCreatedAt(LocalDateTime.now());
                messageMapper.insert(userMsg);

                // Load history
                List<Message> history = loadRecentMessages(conversationId, 20);
                List<Map<String, String>> messages = new ArrayList<>();
                for (Message msg : history) {
                    messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
                }

                // Call LLM with streaming
                LlmProvider.LlmRequest llmRequest = new LlmProvider.LlmRequest(
                        request.getModel() != null ? request.getModel() : "mimo-v2.5-pro",
                        messages,
                        0.7f,
                        2048,
                        null
                );

                StringBuilder fullContent = new StringBuilder();

                llmGatewayService.chatStream(llmRequest, new LlmProvider.StreamCallback() {
                    @Override
                    public void onToken(String token) {
                        try {
                            fullContent.append(token);
                            emitter.send(SseEmitter.event()
                                    .data(Map.of("type", "token", "content", token)));
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onDone(LlmProvider.LlmResponse response) {
                        try {
                            // Save assistant message
                            Message assistantMsg = new Message();
                            assistantMsg.setConvId(conversationId);
                            assistantMsg.setRole("assistant");
                            assistantMsg.setContent(fullContent.toString());
                            assistantMsg.setMetadata(new HashMap<>());
                            assistantMsg.setTokens((int) response.usage()
                                    .getOrDefault("completionTokens", 0));
                            assistantMsg.setCreatedAt(LocalDateTime.now());
                            messageMapper.insert(assistantMsg);

                            emitter.send(SseEmitter.event()
                                    .data(Map.of("type", "done",
                                            "conversationId", conversationId,
                                            "messageId", assistantMsg.getId(),
                                            "usage", response.usage())));
                            emitter.complete();
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .data(Map.of("type", "error", "message", e.getMessage())));
                            emitter.complete();
                        } catch (IOException ex) {
                            emitter.completeWithError(ex);
                        }
                    }
                });

            } catch (Exception e) {
                log.error("Stream chat error", e);
                try {
                    emitter.send(SseEmitter.event()
                            .data(Map.of("type", "error", "message", e.getMessage())));
                    emitter.complete();
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            }
        });

        return emitter;
    }

    public PageResult<Message> getMessages(String conversationId, String userId, int page, int pageSize) {
        conversationService.getConversation(conversationId, userId);

        Page<Message> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<Message>()
                .eq(Message::getConvId, conversationId)
                .orderByAsc(Message::getCreatedAt);

        Page<Message> result = messageMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, pageSize);
    }

    private List<Message> loadRecentMessages(String conversationId, int limit) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<Message>()
                .eq(Message::getConvId, conversationId)
                .orderByDesc(Message::getCreatedAt)
                .last("LIMIT " + limit);
        List<Message> messages = messageMapper.selectList(wrapper);
        Collections.reverse(messages);
        return messages;
    }
}
