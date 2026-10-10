package com.pocketmentor.pocketmentor.dto;
import java.util.List;
public record QuizView(Long quizId, Long noteId, List<QuestionView> questions) {}