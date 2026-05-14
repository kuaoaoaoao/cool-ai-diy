package com.coolai.knowledge.chunker;

import java.util.List;

/**
 * Text Chunker interface - Strategy pattern for multiple chunking strategies
 */
public interface TextChunker {

    String getName();

    List<String> chunk(String text, int chunkSize, int overlap);
}
