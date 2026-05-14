package com.coolai.deepfake.engine;

import java.util.List;
import java.util.Map;

/**
 * Replace Engine interface - Strategy pattern for multiple face replacement backends
 */
public interface ReplaceEngine {

    String getName();

    boolean isAvailable();

    ReplaceResult replace(ReplaceEngineRequest request);

    List<DetectedFace> detectFaces(byte[] videoFrame);

    record ReplaceEngineRequest(
            String sourceVideoPath,
            String targetFacePath,
            int targetFaceIndex,
            String mode,
            String blendMode,
            boolean enhanceFace,
            String outputFormat
    ) {}

    record ReplaceResult(
            String filePath,
            boolean hasWatermark
    ) {}

    record DetectedFace(
            int index,
            float x,
            float y,
            float width,
            float height,
            float confidence
    ) {}
}
