package com.coolai.knowledge.parser;

import java.io.InputStream;
import java.util.List;

/**
 * Document Parser interface - Strategy pattern for multiple document formats
 */
public interface DocumentParser {

    boolean supports(String fileType);

    List<String> parse(InputStream inputStream, String filename);
}
