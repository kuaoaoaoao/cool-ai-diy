package com.coolai.knowledge.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DOCX Document Parser - Parse Word documents using Apache POI
 */
@Slf4j
@Component
public class DocxParser implements DocumentParser {

    @Override
    public boolean supports(String fileType) {
        return "docx".equalsIgnoreCase(fileType) || "doc".equalsIgnoreCase(fileType);
    }

    @Override
    public List<String> parse(InputStream inputStream, String filename) {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            List<String> paragraphs = document.getParagraphs().stream()
                    .map(XWPFParagraph::getText)
                    .filter(text -> !text.trim().isEmpty())
                    .collect(Collectors.toList());
            log.info("Parsed DOCX: {}, paragraphs: {}", filename, paragraphs.size());
            return paragraphs;
        } catch (Exception e) {
            log.error("Failed to parse DOCX: {}", filename, e);
            return Collections.emptyList();
        }
    }
}
