package com.coolai.singing.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Edge-TTS Singing Fallback Engine - reads lyrics aloud when no GPU available
 */
@Slf4j
@Component
public class EdgeTtsSingingEngine implements SingingEngine {

    @Override
    public String getName() {
        return "edge_tts";
    }

    @Override
    public boolean isAvailable() {
        return true; // Always available as fallback
    }

    @Override
    public SingingResult synthesize(SingingRequest request) {
        try {
            String outputPath = "/tmp/singing_edge_" + System.currentTimeMillis() + ".mp3";
            ProcessBuilder pb = new ProcessBuilder(
                    "edge-tts",
                    "--voice", "zh-CN-XiaoxiaoNeural",
                    "--text", request.lyrics(),
                    "--write-media", outputPath
            );
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Edge-TTS singing fallback failed");
            }

            return new SingingResult(null, "mp3", 0, outputPath);
        } catch (Exception e) {
            log.error("Edge-TTS singing fallback failed", e);
            throw new RuntimeException("Singing fallback failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, String>> listStyles() {
        return List.of(Map.of("id", "read", "name", "Read Aloud (No GPU)"));
    }
}
