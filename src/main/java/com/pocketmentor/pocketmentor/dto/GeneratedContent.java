package com.pocketmentor.pocketmentor.dto;
import java.util.List;

public record GeneratedContent(
    String summary,
    List<String> topics,
    List<Card> flashcards,
    List<Q> questions) {
    public record Card(String question, String answer, String topic) {}
    public record Q(String text, List<String> options, String correct, String explanation, String topic) {}
}