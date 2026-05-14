package com.coolai.knowledge.store;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-Memory Vector Store - For development and testing
 */
@Slf4j
@Component
public class InMemoryVectorStore implements VectorStore {

    // collection -> id -> vector
    private final Map<String, Map<String, float[]>> collections = new ConcurrentHashMap<>();
    // collection -> id -> metadata
    private final Map<String, Map<String, String>> metadata = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return "in_memory";
    }

    @Override
    public boolean isAvailable() {
        return true; // Always available
    }

    @Override
    public void createCollection(String collectionName, int dimension) {
        collections.putIfAbsent(collectionName, new ConcurrentHashMap<>());
        metadata.putIfAbsent(collectionName, new ConcurrentHashMap<>());
        log.info("Created in-memory collection: {} (dim={})", collectionName, dimension);
    }

    @Override
    public String insert(String collectionName, String id, float[] vector, String meta) {
        collections.computeIfAbsent(collectionName, k -> new ConcurrentHashMap<>())
                .put(id, vector.clone());
        metadata.computeIfAbsent(collectionName, k -> new ConcurrentHashMap<>())
                .put(id, meta);
        return id;
    }

    @Override
    public List<SearchResult> search(String collectionName, float[] queryVector, int topK) {
        Map<String, float[]> vectors = collections.get(collectionName);
        if (vectors == null || vectors.isEmpty()) {
            return Collections.emptyList();
        }

        List<SearchResult> results = new ArrayList<>();
        for (Map.Entry<String, float[]> entry : vectors.entrySet()) {
            float similarity = cosineSimilarity(queryVector, entry.getValue());
            String meta = metadata.getOrDefault(collectionName, Collections.emptyMap())
                    .getOrDefault(entry.getKey(), "");
            results.add(new SearchResult(entry.getKey(), similarity, meta));
        }

        return results.stream()
                .sorted(Comparator.comparingDouble(SearchResult::score).reversed())
                .limit(topK)
                .toList();
    }

    @Override
    public void delete(String collectionName, List<String> ids) {
        Map<String, float[]> vectors = collections.get(collectionName);
        Map<String, String> meta = metadata.get(collectionName);
        if (vectors != null) {
            ids.forEach(vectors::remove);
        }
        if (meta != null) {
            ids.forEach(meta::remove);
        }
    }

    @Override
    public void dropCollection(String collectionName) {
        collections.remove(collectionName);
        metadata.remove(collectionName);
        log.info("Dropped in-memory collection: {}", collectionName);
    }

    private float cosineSimilarity(float[] a, float[] b) {
        float dotProduct = 0, normA = 0, normB = 0;
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0;
        return dotProduct / (float) (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
