package com.pocketmentor.pocketmentor.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record SubmitRequest(@NotEmpty List<@Valid Item> answers) {
    public record Item(@NotNull Long questionId, @NotBlank String selected, @NotBlank String confidence) {}
}