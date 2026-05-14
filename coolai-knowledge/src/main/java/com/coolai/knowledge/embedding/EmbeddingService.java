package com.coolai.knowledge.embedding;

import java.util.List;

/**
 * Embedding Service interface - Strategy pattern for embedding providers
 */
public interface EmbeddingService {

    String getName();

    boolean isAvailable();

    List<float[]> embed(List<String> texts);

    int getDimension();
}
