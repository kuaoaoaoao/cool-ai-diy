package com.coolai.knowledge.chunker;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RecursiveChunker implements TextChunker {

    private static final String[] SEPARATORS = {"\n\n", "\n", ". ", " ", ""};

    @Override
    public String getName() {
        return "recursive";
    }

    @Override
    public List<String> chunk(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        recursiveSplit(text, chunkSize, overlap, 0, chunks);
        return chunks;
    }

    private void recursiveSplit(String text, int chunkSize, int overlap, int separatorIndex, List<String> chunks) {
        if (text.length() <= chunkSize || separatorIndex >= SEPARATORS.length) {
            if (!text.trim().isEmpty()) {
                chunks.add(text.trim());
            }
            return;
        }

        String separator = SEPARATORS[separatorIndex];
        String[] parts = text.split(separator);

        StringBuilder current = new StringBuilder();
        for (String part : parts) {
            if (current.length() + part.length() + separator.length() > chunkSize && current.length() > 0) {
                chunks.add(current.toString().trim());
                // Handle overlap
                if (overlap > 0 && current.length() > overlap) {
                    String overlapText = current.substring(current.length() - overlap);
                    current = new StringBuilder(overlapText);
                } else {
                    current = new StringBuilder();
                }
            }
            if (current.length() > 0) {
                current.append(separator);
            }
            current.append(part);
        }

        if (current.length() > 0) {
            if (current.length() > chunkSize) {
                recursiveSplit(current.toString(), chunkSize, overlap, separatorIndex + 1, chunks);
            } else {
                chunks.add(current.toString().trim());
            }
        }
    }
}
