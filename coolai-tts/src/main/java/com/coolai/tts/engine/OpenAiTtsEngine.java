package com.coolai.tts.engine;

import com.coolai.model.dto.TtsRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OpenAI TTS Engine - Use OpenAI TTS API for high-quality speech synthesis
 */
@Slf4j
@Component
public class OpenAiTtsEngine implements TtsEngine {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${coolai.llm.openai.api-key:}")
    private String apiKey;

    @Value("${coolai.llm.openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    public OpenAiTtsEngine(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public String getName() { return "openai_tts"; }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.startsWith("fake-");
    }

    @Override
    public TtsResult synthesize(TtsRequest request) {
        try {
            Map<String, Object> body = Map.of(
                    "model", "tts-1",
                    "input", request.getText(),
                    "voice", request.getVoiceId() != null ? request.getVoiceId() : "alloy",
                    "response_format", request.getFormat() != null ? request.getFormat() : "mp3"
            );
            String json = objectMapper.writeValueAsString(body);
            Request httpRequest = new Request.Builder()
                    .url(baseUrl + "/audio/speech")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .post(okhttp3.RequestBody.create(json, okhttp3.MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) throw new RuntimeException("OpenAI TTS error: " + response.code());
                String outputPath = "/tmp/tts_openai_" + System.currentTimeMillis() + ".mp3";
                java.nio.file.Files.write(java.nio.file.Path.of(outputPath), response.body().bytes());
                return new TtsResult(null, "mp3", 0, outputPath);
            }
        } catch (Exception e) {
            log.error("OpenAI TTS synthesis failed", e);
            throw new RuntimeException("OpenAI TTS failed: " + e.getMessage(), e);
        }
    }

    @Override
    public SseEmitter synthesizeStream(TtsRequest request) {
        SseEmitter emitter = new SseEmitter(120_000L);
        try {
            TtsResult result = synthesize(request);
            emitter.send(SseEmitter.event().data(Map.of("type", "done", "filePath", result.filePath())));
            emitter.complete();
        } catch (Exception e) { emitter.completeWithError(e); }
        return emitter;
    }

    @Override
    public List<Map<String, Object>> listVoices() {
        return List.of(
                Map.of("id", "alloy", "name", "Alloy", "language", "multi"),
                Map.of("id", "echo", "name", "Echo (Male)", "language", "multi"),
                Map.of("id", "fable", "name", "Fable", "language", "multi"),
                Map.of("id", "onyx", "name", "Onyx (Male)", "language", "multi"),
                Map.of("id", "nova", "name", "Nova (Female)", "language", "multi"),
                Map.of("id", "shimmer", "name", "Shimmer (Female)", "language", "multi")
        );
    }
}
