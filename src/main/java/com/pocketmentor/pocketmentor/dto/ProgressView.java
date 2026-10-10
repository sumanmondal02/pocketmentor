package com.pocketmentor.pocketmentor.dto;
import java.time.LocalDateTime;
import java.util.List;

public record ProgressView(int totalAnswered, int overallAccuracy, int blindSpotTotal,
                           List<TopicStat> topics, List<HistoryItem> history) {
    public record TopicStat(String topic, long answered, long correct, long blindSpots, int accuracy, String mastery) {}
    public record HistoryItem(Long attemptId, Long quizId, String noteTitle, int score, int total, LocalDateTime takenAt) {}
}