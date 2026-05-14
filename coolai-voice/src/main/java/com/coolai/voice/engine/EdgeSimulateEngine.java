package com.coolai.voice.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Edge-TTS Simulation Engine - Fallback when GPU is not available
 */
@Slf4j
@Component
public class EdgeSimulateEngine implements CloneEngine {

    @Override
    public String getName() {
        return "edge_simulate";
    }

    @Override
    public boolean isAvailable() {
        return true; // Always available as fallback
    }

    @Override
    public CloneResult synthesize(CloneRequest request) {
        // Use Edge-TTS to simulate voice cloning with voice mapping
        try {
            String outputPath = "/tmp/clone_" + System.currentTimeMillis() + ".mp3";
            ProcessBuilder pb = new ProcessBuilder(
                    "edge-tts",
                    "--voice", "zh-CN-XiaoxiaoNeural",
                    "--text", request.text(),
                    "--write-media", outputPath
            );
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Edge-TTS simulation failed");
            }

            return new CloneResult(null, "mp3", outputPath);
        } catch (Exception e) {
            log.error("Edge-TTS simulation failed", e);
            throw new RuntimeException("Voice synthesis failed: " + e.getMessage(), e);
        }
    }
}
