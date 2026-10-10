package com.pocketmentor.pocketmentor.dto;
import java.util.List;

public record AttemptResult(Long attemptId, int score, int total, int blindSpotCount, List<Item> items) {
    public record Item(Long questionId, String text, String selected, String correctOption, String correctText,
                       String explanation, String confidence, boolean correct, boolean blindSpot, String topic) {}
}