package com.coolai.knowledge.chunker;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Paragraph Chunker - Split text by paragraphs, respecting natural boundaries
 */
@Component
public class ParagraphChunker implements TextChunker {

    @Override
    public String getName() {
        return "paragraph";
    }

    @Override
    public List<String> chunk(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        String[] paragraphs = text.split("\\n\\s*\\n");

        StringBuilder currentChunk = new StringBuilder();
        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty()) continue;

            if (currentChunk.length() + trimmed.length() + 2 > chunkSize && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());
                if (overlap > 0 && currentChunk.length() > overlap) {
                    String overlapText = currentChunk.substring(currentChunk.length() - overlap);
                    currentChunk = new StringBuilder(overlapText);
                } else {
                    currentChunk = new StringBuilder();
                }
            }
            if (currentChunk.length() > 0) {
                currentChunk.append("\n\n");
            }
            currentChunk.append(trimmed);
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }
        return chunks;
    }
}
