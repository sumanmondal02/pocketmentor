package com.pocketmentor.pocketmentor.dto;
import jakarta.validation.constraints.*;
public record RegisterRequest(
    @NotBlank String name,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password) {}