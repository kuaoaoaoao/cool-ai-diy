package com.coolai.knowledge.parser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TxtParser implements DocumentParser {

    @Override
    public boolean supports(String fileType) {
        return "txt".equalsIgnoreCase(fileType);
    }

    @Override
    public List<String> parse(InputStream inputStream, String filename) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String content = reader.lines().collect(Collectors.joining("\n"));
            return List.of(content);
        } catch (Exception e) {
            log.error("Failed to parse TXT: {}", filename, e);
            return Collections.emptyList();
        }
    }
}
