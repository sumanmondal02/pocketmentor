package com.pocketmentor.pocketmentor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocketmentor.pocketmentor.dto.GeneratedContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
public class GeminiAiService implements AiService {
    private static final int MAX_ATTEMPTS = 3;
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

    private final RestClient http;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey;
    private final String model;

    public GeminiAiService(@Value("${ai.api-key}") String apiKey, @Value("${ai.model}") String model) {
        this.apiKey = apiKey;
        this.model = model;
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
        factory.setReadTimeout(Duration.ofSeconds(90));
        this.http = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public GeneratedContent generate(String notesText) {
        if (apiKey == null || apiKey.isBlank())
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI key not configured");

        Map<String, Object> body = Map.of(
            "contents", new Object[]{Map.of("parts", new Object[]{Map.of("text", PROMPT + notesText)})},
            "generationConfig", Map.of("responseMimeType", "application/json"));

        String resp = callWithRetry(body);

        try {
            JsonNode root = mapper.readTree(resp);
            String json = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
            json = json.replaceAll("^```json|^```|```$", "").trim();
            GeneratedContent g = mapper.readValue(json, GeneratedContent.class);
            if (g.flashcards() == null || g.questions() == null || g.topics() == null || g.summary() == null)
                throw new IllegalStateException("Incomplete AI response");
            return g;
        } catch (Exception e) {
            log.warn("AI response could not be parsed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI returned an unreadable answer. Please try again.");
        }
    }

    private String callWithRetry(Map<String, Object> body) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return http.post()
                    .uri("https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent")
                    .header("x-goog-api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .body(body).retrieve().body(String.class);
            } catch (RestClientResponseException e) {
                int code = e.getStatusCode().value();
                boolean retryable = code == 429 || code >= 500;
                log.warn("Gemini HTTP {} (attempt {}/{})", code, attempt, MAX_ATTEMPTS);
                if (!retryable || attempt == MAX_ATTEMPTS)
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "AI service is busy or unavailable (" + code + "). Please try again shortly.");
            } catch (ResourceAccessException e) {   // timeouts / network
                log.warn("Gemini network error (attempt {}/{}): {}", attempt, MAX_ATTEMPTS, e.getMessage());
                if (attempt == MAX_ATTEMPTS)
                    throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "AI service timed out. Please try again.");
            }
            try { Thread.sleep(2000L * attempt); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        }
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI generation failed");
    }
}