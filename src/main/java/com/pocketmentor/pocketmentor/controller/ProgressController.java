package com.pocketmentor.pocketmentor.controller;

import com.pocketmentor.pocketmentor.dto.*;
import com.pocketmentor.pocketmentor.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProgressController {
    private final ProgressService service;

    @GetMapping("/progress")
    public ProgressView progress(Authentication a) { return service.progress(a.getName()); }

    @PostMapping("/notes/{id}/retake-weak")
    public QuizView retake(@PathVariable Long id, Authentication a) { return service.retakeWeak(a.getName(), id); }
}