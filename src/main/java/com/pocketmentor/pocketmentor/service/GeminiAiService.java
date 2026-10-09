package com.pocketmentor.pocketmentor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocketmentor.pocketmentor.dto.GeneratedContent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@Service
public class GeminiAiService implements AiService {
    private static final String PROMPT = """
        You are a study assistant. From the student's notes below, produce STRICT JSON only, with this shape:
        {"summary":"60-second summary, max 150 words",
         "topics":["3-6 short topic names"],
         "flashcards":[{"question":"","answer":"","topic":"one of topics"}],
         "questions":[{"text":"","options":["A text","B text","C text","D text"],"correct":"A|B|C|D","explanation":"","topic":"one of topics"}]}
        Rules: 8-12 flashcards, 6-10 multiple-choice questions with exactly 4 options (no letter prefixes in option text),
        base everything only on the notes, vary which letter is correct.
        NOTES:
        """;

    private final RestClient http = RestClient.create();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey;
    private final String model;

    public GeminiAiService(@Value("${ai.api-key}") String apiKey, @Value("${ai.model}") String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public GeneratedContent generate(String notesText) {
        if (apiKey == null || apiKey.isBlank())
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI key not configured");
        Map<String, Object> body = Map.of(
            "contents", new Object[]{Map.of("parts", new Object[]{Map.of("text", PROMPT + notesText)})},
            "generationConfig", Map.of("responseMimeType", "application/json"));
        try {
            String resp = http.post()
                .uri("https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent")
                .header("x-goog-api-key", apiKey)
                .header("Content-Type", "application/json")
                .body(body).retrieve().body(String.class);
            JsonNode root = mapper.readTree(resp);
            String json = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
            json = json.replaceAll("^```json|^```|```$", "").trim();
            GeneratedContent g = mapper.readValue(json, GeneratedContent.class);
            if (g.flashcards() == null || g.questions() == null || g.topics() == null)
                throw new IllegalStateException("Incomplete AI response");
            return g;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI generation failed: " + e.getMessage());
        }
    }
}