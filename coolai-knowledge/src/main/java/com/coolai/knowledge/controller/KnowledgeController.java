package com.coolai.knowledge.controller;

import com.coolai.common.model.ApiResponse;
import com.coolai.common.model.PageResult;
import com.coolai.knowledge.service.KnowledgeService;
import com.coolai.knowledge.service.RagPipeline;
import com.coolai.model.entity.Document;
import com.coolai.model.entity.KnowledgeBase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/knowledge")
@RequiredArgsConstructor
@Tag(name = "Knowledge", description = "RAG Knowledge base APIs")
public class KnowledgeController {

    private final RagPipeline ragPipeline;
    private final KnowledgeService knowledgeService;

    @PostMapping("/bases")
    @Operation(summary = "Create knowledge base")
    public ApiResponse<KnowledgeBase> createKnowledgeBase(@RequestBody Map<String, String> request) {
        String userId = getCurrentUserId();
        return ApiResponse.ok(knowledgeService.createKnowledgeBase(
                userId,
                request.getOrDefault("name", "New KB"),
                request.getOrDefault("description", "")
        ));
    }

    @GetMapping("/bases")
    @Operation(summary = "List knowledge bases")
    public ApiResponse<PageResult<KnowledgeBase>> listKnowledgeBases(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        String userId = getCurrentUserId();
        return ApiResponse.ok(knowledgeService.listKnowledgeBases(userId, page, pageSize));
    }

    @GetMapping("/bases/{id}/documents")
    @Operation(summary = "List documents in knowledge base")
    public ApiResponse<PageResult<Document>> listDocuments(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        String userId = getCurrentUserId();
        return ApiResponse.ok(knowledgeService.listDocuments(id, userId, page, pageSize));
    }

    @PostMapping("/bases/{id}/documents")
    @Operation(summary = "Upload document to knowledge base")
    public ApiResponse<Document> uploadDocument(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        String userId = getCurrentUserId();
        Document document = null;
        try {
            String filename = file.getOriginalFilename();
            String fileType = filename != null ?
                    filename.substring(filename.lastIndexOf(".") + 1) : "txt";
            document = knowledgeService.createDocument(id, userId, filename, fileType, file.getSize());
            int chunkCount = ragPipeline.indexDocument(file.getBytes(), filename, fileType, id, document.getId());
            knowledgeService.markDocumentProcessed(document.getId(), chunkCount);
            document.setStatus("processed");
            return ApiResponse.ok(document);
        } catch (Exception e) {
            if (document != null) {
                knowledgeService.markDocumentFailed(document.getId());
                document.setStatus("failed");
                return ApiResponse.ok(document);
            }
            return ApiResponse.fail("Failed to upload document: " + e.getMessage());
        }
    }

    @PostMapping("/bases/{id}/query")
    @Operation(summary = "RAG query")
    public ApiResponse<RagPipeline.RagResponse> query(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        String userId = getCurrentUserId();
        knowledgeService.getKnowledgeBase(id, userId);
        String query = request.get("query");
        String model = request.getOrDefault("model", "mimo-v2.5-pro");
        return ApiResponse.ok(ragPipeline.query(query, id, model));
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (String) authentication.getPrincipal();
    }
}
