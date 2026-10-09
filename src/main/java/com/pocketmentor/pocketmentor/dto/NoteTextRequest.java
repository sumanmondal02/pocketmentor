package com.pocketmentor.pocketmentor.dto;
import jakarta.validation.constraints.*;
public record NoteTextRequest(String title, @NotBlank @Size(max = 100000) String text) {}