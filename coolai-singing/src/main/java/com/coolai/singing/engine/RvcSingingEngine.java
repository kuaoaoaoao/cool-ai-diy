package com.coolai.singing.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class RvcSingingEngine implements SingingEngine {

    @Value("${coolai.singing.rvc.enabled:false}")
    private boolean enabled;

    @Value("${coolai.singing.rvc.script-path:/opt/rvc/inference.py}")
    private String scriptPath;

    @Override
    public String getName() {
        return "rvc";
    }

    @Override
    public boolean isAvailable() {
        return enabled;
    }

    @Override
    public SingingResult synthesize(SingingRequest request) {
        try {
            String outputPath = "/tmp/singing_rvc_" + System.currentTimeMillis() + ".wav";
            ProcessBuilder pb = new ProcessBuilder(
                    "python3", scriptPath,
                    "--lyrics", request.lyrics(),
                    "--voice-model", request.voiceModelId(),
                    "--style", request.style(),
                    "--output", outputPath
            );
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("RVC singing inference failed");
            }

            return new SingingResult(null, "wav", 0, outputPath);
        } catch (Exception e) {
            log.error("RVC singing synthesis failed", e);
            throw new RuntimeException("RVC singing failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, String>> listStyles() {
        return List.of(
                Map.of("id", "pop", "name", "Pop"),
                Map.of("id", "opera", "name", "Opera"),
                Map.of("id", "rap", "name", "Rap"),
                Map.of("id", "ballad", "name", "Ballad")
        );
    }
}
