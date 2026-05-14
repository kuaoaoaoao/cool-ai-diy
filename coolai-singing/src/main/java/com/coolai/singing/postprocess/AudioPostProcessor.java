package com.coolai.singing.postprocess;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Audio Post-Processor - Apply effects like reverb, EQ, and noise reduction
 * Uses TarsosDSP library (when available) or FFmpeg subprocess
 */
@Slf4j
@Service
public class AudioPostProcessor {

    /**
     * Apply reverb effect to audio file
     */
    public String applyReverb(String inputPath, float reverbAmount) {
        log.info("Applying reverb: input={}, amount={}", inputPath, reverbAmount);
        try {
            String outputPath = inputPath.replace(".wav", "_reverb.wav").replace(".mp3", "_reverb.mp3");
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-i", inputPath,
                    "-af", "aecho=0.8:0.88:" + (reverbAmount * 60) + ":" + reverbAmount,
                    "-y", outputPath
            );
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.warn("FFmpeg reverb failed, returning original");
                return inputPath;
            }
            return outputPath;
        } catch (Exception e) {
            log.warn("Reverb failed, returning original: {}", e.getMessage());
            return inputPath;
        }
    }

    /**
     * Apply equalizer to audio file
     */
    public String applyEqualizer(String inputPath, float bassGain, float midGain, float trebleGain) {
        log.info("Applying EQ: input={}", inputPath);
        try {
            String outputPath = inputPath.replace(".wav", "_eq.wav").replace(".mp3", "_eq.mp3");
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-i", inputPath,
                    "-af", String.format("equalizer=f=200:t=q:w=1:g=%.1f,equalizer=f=1000:t=q:w=1:g=%.1f,equalizer=f=5000:t=q:w=1:g=%.1f",
                            bassGain, midGain, trebleGain),
                    "-y", outputPath
            );
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) return inputPath;
            return outputPath;
        } catch (Exception e) {
            return inputPath;
        }
    }

    /**
     * Apply noise reduction
     */
    public String applyNoiseReduction(String inputPath) {
        log.info("Applying noise reduction: input={}", inputPath);
        // Would use TarsosDSP or FFmpeg noise filter
        return inputPath;
    }
}
