package com.pocketmentor.pocketmentor.dto;
import java.time.LocalDateTime;
public record NoteSummary(Long id, String title, String sourceType, LocalDateTime createdAt,
                          String status, long flashcards, long questions) {}