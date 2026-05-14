package com.coolai.knowledge.embedding;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Local Embedding Service - Mock/local embedding for development
 * In production, would use DJL (Deep Java Library) with ONNX models
 */
@Slf4j
@Component
public class LocalEmbeddingService implements EmbeddingService {

    private static final int DIMENSION = 384;
    private final Random random = new Random(42);

    @Override
    public String getName() {
        return "local";
    }

    @Override
    public boolean isAvailable() {
        return true; // Always available as fallback
    }

    @Override
    public List<float[]> embed(List<String> texts) {
        log.info("Local embedding: {} texts (mock implementation)", texts.size());
        // Mock: generate random normalized vectors
        // In production, would use DJL with sentence-transformers model
        return texts.stream()
                .map(text -> generateRandomVector())
                .collect(Collectors.toList());
    }

    @Override
    public int getDimension() {
        return DIMENSION;
    }

    private float[] generateRandomVector() {
        float[] vector = new float[DIMENSION];
        float sum = 0;
        for (int i = 0; i < DIMENSION; i++) {
            vector[i] = (float) random.nextGaussian();
            sum += vector[i] * vector[i];
        }
        // Normalize
        float norm = (float) Math.sqrt((double) sum);
        for (int i = 0; i < DIMENSION; i++) {
            vector[i] /= norm;
        }
        return vector;
    }
}
