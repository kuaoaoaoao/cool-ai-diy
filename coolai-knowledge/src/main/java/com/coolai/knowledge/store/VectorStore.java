package com.coolai.knowledge.store;

import java.util.List;

/**
 * Vector Store interface - Strategy pattern for vector storage backends
 */
public interface VectorStore {

    String getName();

    boolean isAvailable();

    void createCollection(String collectionName, int dimension);

    String insert(String collectionName, String id, float[] vector, String metadata);

    List<SearchResult> search(String collectionName, float[] queryVector, int topK);

    void delete(String collectionName, List<String> ids);

    void dropCollection(String collectionName);

    record SearchResult(String id, float score, String metadata) {}
}
