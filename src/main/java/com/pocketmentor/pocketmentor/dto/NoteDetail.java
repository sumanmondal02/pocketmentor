package com.pocketmentor.pocketmentor.dto;
import java.time.LocalDateTime;
public record NoteDetail(Long id, String title, String sourceType, LocalDateTime createdAt, String text) {}