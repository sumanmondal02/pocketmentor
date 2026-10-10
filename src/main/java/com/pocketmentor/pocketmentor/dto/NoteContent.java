package com.pocketmentor.pocketmentor.dto;
import java.util.List;
public record NoteContent(String summary, List<String> topics, Long quizId) {}