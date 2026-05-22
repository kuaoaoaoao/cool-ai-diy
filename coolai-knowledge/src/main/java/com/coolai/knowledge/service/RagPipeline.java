package com.coolai.knowledge.service;

import com.coolai.common.llm.LlmGatewayService;
import com.coolai.common.llm.LlmProvider;
import com.coolai.knowledge.chunker.TextChunker;
import com.coolai.knowledge.mapper.ChunkMapper;
import com.coolai.knowledge.mapper.DocumentMapper;
import com.coolai.knowledge.parser.DocumentParser;
import com.coolai.model.entity.Chunk;
import com.coolai.model.entity.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagPipeline {

    private final LlmGatewayService llmGatewayService;
    private final List<DocumentParser> parsers;
    private final List<TextChunker> chunkers;
    private final ChunkMapper chunkMapper;
    private final DocumentMapper documentMapper;

    /**
     * RAG Query: Retrieve relevant chunks and generate answer
     */
    public RagResponse query(String query, String knowledgeBaseId, String model) {
        // 1. Retrieve relevant chunks (simplified - would use vector store in production)
        List<String> relevantChunks = retrieve(query, knowledgeBaseId);

        // 2. Build prompt with context
        String context = relevantChunks.stream()
                .collect(Collectors.joining("\n---\n"));

        String prompt = """
                Based on the following context, answer the user's question.
                If the answer cannot be found in the context, say so.
                
                Context:
                %s
                
                Question: %s
                """.formatted(context, query);

        // 3. Call LLM to generate answer
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", "You are a helpful assistant that answers questions based on the provided context."),
                Map.of("role", "user", "content", prompt)
        );

        LlmProvider.LlmRequest request = new LlmProvider.LlmRequest(
                model != null ? model : "mimo-v2.5-pro",
                messages,
                0.7f,
                2048,
                null
        );

        LlmProvider.LlmResponse response = llmGatewayService.chat(request);

        return new RagResponse(response.content(), relevantChunks, response.usage());
    }

    /**
     * Retrieve relevant chunks for a query (simplified)
     */
    private List<String> retrieve(String query, String knowledgeBaseId) {
        log.info("Retrieving chunks for query in KB: {}", knowledgeBaseId);
        List<String> docIds = documentMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Document>()
                        .eq(Document::getKbId, knowledgeBaseId))
                .stream()
                .map(Document::getId)
                .toList();
        if (docIds.isEmpty()) {
            return List.of("The knowledge base has no indexed documents yet.");
        }

        List<Chunk> chunks = chunkMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Chunk>()
                .in(Chunk::getDocId, docIds)
                .orderByAsc(Chunk::getChunkIndex)
                .last("LIMIT 8"));

        if (chunks.isEmpty()) {
            return List.of("The knowledge base has no indexed chunks yet.");
        }

        String normalizedQuery = query == null ? "" : query.toLowerCase(Locale.ROOT);
        return chunks.stream()
                .sorted(Comparator.comparingInt(chunk -> scoreChunk(chunk.getContent(), normalizedQuery)))
                .map(Chunk::getContent)
                .limit(5)
                .toList();
    }

    /**
     * Index a document: parse → chunk → embed → store
     */
    public int indexDocument(byte[] fileData, String filename, String fileType, String knowledgeBaseId, String documentId) {
        // 1. Parse document
        DocumentParser parser = parsers.stream()
                .filter(p -> p.supports(fileType))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unsupported file type: " + fileType));

        List<String> paragraphs = parser.parse(new java.io.ByteArrayInputStream(fileData), filename);

        // 2. Chunk text
        TextChunker chunker = chunkers.stream()
                .filter(c -> c.getName().equals("recursive"))
                .findFirst()
                .orElse(chunkers.get(0));

        String fullText = String.join("\n", paragraphs);
        List<String> chunks = chunker.chunk(fullText, 512, 50);

        // 3. Generate embeddings
        List<float[]> embeddings = llmGatewayService.embeddings(chunks);

        // 4. Store in vector store (simplified)
        log.info("Indexed {} chunks for document: {}", chunks.size(), filename);
        for (int i = 0; i < chunks.size(); i++) {
            Chunk chunk = new Chunk();
            chunk.setDocId(documentId);
            chunk.setContent(chunks.get(i));
            chunk.setChunkIndex(i);
            chunk.setVectorId("local-" + documentId + "-" + i);
            chunk.setMetadata(Map.of(
                    "filename", filename,
                    "fileType", fileType,
                    "embeddingDimension", embeddings.get(i).length
            ));
            chunk.setCreatedAt(LocalDateTime.now());
            chunkMapper.insert(chunk);
        }
        return chunks.size();
    }

    public record RagResponse(String answer, List<String> sources, Map<String, Object> usage) {}

    private int scoreChunk(String content, String normalizedQuery) {
        if (content == null || normalizedQuery.isBlank()) {
            return Integer.MAX_VALUE;
        }
        String lower = content.toLowerCase(Locale.ROOT);
        return lower.contains(normalizedQuery) ? 0 : Math.abs(lower.length() - normalizedQuery.length());
    }
}
