package com.coolai.chat.controller;

import com.coolai.chat.service.ChatService;
import com.coolai.chat.service.ConversationService;
import com.coolai.common.model.ApiResponse;
import com.coolai.common.model.PageResult;
import com.coolai.model.dto.ChatRequest;
import com.coolai.model.dto.ChatResponse;
import com.coolai.model.entity.Conversation;
import com.coolai.model.entity.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "AI Chat APIs")
public class ChatController {

    private final ChatService chatService;
    private final ConversationService conversationService;

    @PostMapping("/conversations")
    @Operation(summary = "Create a new conversation")
    public ApiResponse<Conversation> createConversation(@RequestParam(required = false) String title,
                                                        @RequestParam(required = false) String model) {
        String userId = getCurrentUserId();
        return ApiResponse.ok(conversationService.createConversation(userId, title, model));
    }

    @GetMapping("/conversations")
    @Operation(summary = "List conversations")
    public ApiResponse<PageResult<Conversation>> listConversations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        String userId = getCurrentUserId();
        return ApiResponse.ok(conversationService.listConversations(userId, page, pageSize));
    }

    @GetMapping("/conversations/{id}")
    @Operation(summary = "Get conversation details")
    public ApiResponse<Conversation> getConversation(@PathVariable String id) {
        String userId = getCurrentUserId();
        return ApiResponse.ok(conversationService.getConversation(id, userId));
    }

    @DeleteMapping("/conversations/{id}")
    @Operation(summary = "Delete conversation")
    public ApiResponse<Void> deleteConversation(@PathVariable String id) {
        String userId = getCurrentUserId();
        conversationService.deleteConversation(id, userId);
        return ApiResponse.ok();
    }

    @PostMapping
    @Operation(summary = "Send a chat message (synchronous)")
    public ApiResponse<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        String userId = getCurrentUserId();
        return ApiResponse.ok(chatService.chat(userId, request));
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Send a chat message (SSE streaming)")
    public SseEmitter chatStream(@Valid @RequestBody ChatRequest request) {
        String userId = getCurrentUserId();
        return chatService.chatStream(userId, request);
    }

    @GetMapping("/conversations/{id}/messages")
    @Operation(summary = "Get message history")
    public ApiResponse<PageResult<Message>> getMessages(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        String userId = getCurrentUserId();
        return ApiResponse.ok(chatService.getMessages(id, userId, page, pageSize));
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (String) authentication.getPrincipal();
    }
}
