package com.coolai.singing.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * DiffSinger Engine - MIDI+lyrics driven singing synthesis (requires GPU)
 */
@Slf4j
@Component
public class DiffSingerEngine implements SingingEngine {

    @Value("${coolai.singing.diffsinger.enabled:false}")
    private boolean enabled;

    @Value("${coolai.singing.diffsinger.script-path:/opt/diffsinger/inference.py}")
    private String scriptPath;

    @Override
    public String getName() { return "diff_singer"; }

    @Override
    public boolean isAvailable() { return enabled; }

    @Override
    public SingingResult synthesize(SingingRequest request) {
        try {
            String outputPath = "/tmp/singing_ds_" + System.currentTimeMillis() + ".wav";
            ProcessBuilder pb = new ProcessBuilder(
                    "python3", scriptPath,
                    "--lyrics", request.lyrics(),
                    "--style", request.style() != null ? request.style() : "pop",
                    "--output", outputPath
            );
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) throw new RuntimeException("DiffSinger inference failed");
            return new SingingResult(null, "wav", 0, outputPath);
        } catch (Exception e) {
            log.error("DiffSinger synthesis failed", e);
            throw new RuntimeException("DiffSinger failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, String>> listStyles() {
        return List.of(
                Map.of("id", "pop", "name", "Pop"),
                Map.of("id", "rock", "name", "Rock"),
                Map.of("id", "ballad", "name", "Ballad"),
                Map.of("id", "opera", "name", "Opera")
        );
    }
}
