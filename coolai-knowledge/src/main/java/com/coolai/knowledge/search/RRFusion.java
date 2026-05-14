package com.coolai.knowledge.search;

import java.util.*;

/**
 * Reciprocal Rank Fusion (RRF) for hybrid search result merging
 */
public class RRFusion {

    private static final int DEFAULT_K = 60;

    public static List<ScoredDocument> fuse(List<List<ScoredDocument>> resultLists, int k) {
        Map<String, Double> scores = new HashMap<>();

        for (List<ScoredDocument> resultList : resultLists) {
            for (int i = 0; i < resultList.size(); i++) {
                ScoredDocument doc = resultList.get(i);
                double rrfScore = 1.0 / (k + i + 1);
                scores.merge(doc.id(), rrfScore, Double::sum);
            }
        }

        return scores.entrySet().stream()
                .map(e -> new ScoredDocument(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingDouble(ScoredDocument::score).reversed())
                .toList();
    }

    public static List<ScoredDocument> fuse(List<List<ScoredDocument>> resultLists) {
        return fuse(resultLists, DEFAULT_K);
    }

    public record ScoredDocument(String id, double score) {}
}
