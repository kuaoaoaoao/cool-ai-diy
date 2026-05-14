package com.coolai.knowledge.chunker;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FixedSizeChunker implements TextChunker {

    @Override
    public String getName() {
        return "fixed_size";
    }

    @Override
    public List<String> chunk(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
            start += chunkSize - overlap;
            if (start >= text.length()) break;
        }
        return chunks;
    }
}
