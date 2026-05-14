package com.coolai.knowledge.controller;

import com.coolai.common.model.ApiResponse;
import com.coolai.knowledge.service.RagPipeline;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/knowledge")
@RequiredArgsConstructor
@Tag(name = "Knowledge", description = "RAG Knowledge base APIs")
public class KnowledgeController {

    private final RagPipeline ragPipeline;

    @PostMapping("/bases")
    @Operation(summary = "Create knowledge base")
    public ApiResponse<Map<String, Object>> createKnowledgeBase(@RequestBody Map<String, String> request) {
        return ApiResponse.ok(Map.of("id", "kb_" + System.currentTimeMillis(),
                "name", request.getOrDefault("name", "New KB"),
                "status", "created"));
    }

    @PostMapping("/bases/{id}/documents")
    @Operation(summary = "Upload document to knowledge base")
    public ApiResponse<Map<String, Object>> uploadDocument(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            String fileType = filename != null ?
                    filename.substring(filename.lastIndexOf(".") + 1) : "txt";
            ragPipeline.indexDocument(file.getBytes(), filename, fileType, id);
            return ApiResponse.ok(Map.of("status", "processing", "filename", filename));
        } catch (Exception e) {
            return ApiResponse.fail("Failed to upload document: " + e.getMessage());
        }
    }

    @PostMapping("/bases/{id}/query")
    @Operation(summary = "RAG query")
    public ApiResponse<RagPipeline.RagResponse> query(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        String query = request.get("query");
        String model = request.getOrDefault("model", "gpt-3.5-turbo");
        return ApiResponse.ok(ragPipeline.query(query, id, model));
    }
}
