package com.coolai.knowledge.parser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class PdfParser implements DocumentParser {

    @Override
    public boolean supports(String fileType) {
        return "pdf".equalsIgnoreCase(fileType);
    }

    @Override
    public List<String> parse(InputStream inputStream, String filename) {
        try {
            // Use Apache PDFBox for PDF parsing
            org.apache.pdfbox.Loader.loadPDF(inputStream.readAllBytes());
            // Simplified - actual implementation would extract text from each page
            log.info("Parsing PDF: {}", filename);
            return List.of("PDF content extracted from: " + filename);
        } catch (Exception e) {
            log.error("Failed to parse PDF: {}", filename, e);
            return Collections.emptyList();
        }
    }
}
