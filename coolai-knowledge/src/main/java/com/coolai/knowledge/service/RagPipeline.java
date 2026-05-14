package com.coolai.knowledge.service;

import com.coolai.common.llm.LlmGatewayService;
import com.coolai.common.llm.LlmProvider;
import com.coolai.knowledge.chunker.TextChunker;
import com.coolai.knowledge.parser.DocumentParser;
import com.coolai.knowledge.search.RRFusion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagPipeline {

    private final LlmGatewayService llmGatewayService;
    private final List<DocumentParser> parsers;
    private final List<TextChunker> chunkers;

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
                model != null ? model : "gpt-3.5-turbo",
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
        // In production, this would use Milvus vector store for semantic search
        // and Elasticsearch for BM25 text search, then fuse with RRF
        log.info("Retrieving chunks for query in KB: {}", knowledgeBaseId);
        return List.of("Sample chunk 1", "Sample chunk 2");
    }

    /**
     * Index a document: parse → chunk → embed → store
     */
    public void indexDocument(byte[] fileData, String filename, String fileType, String knowledgeBaseId) {
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
    }

    public record RagResponse(String answer, List<String> sources, Map<String, Object> usage) {}
}
