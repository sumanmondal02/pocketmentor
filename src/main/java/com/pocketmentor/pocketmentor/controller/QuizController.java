package com.pocketmentor.pocketmentor.controller;

import com.pocketmentor.pocketmentor.dto.*;
import com.pocketmentor.pocketmentor.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService service;

    @GetMapping("/notes/{id}/content")
    public NoteContent content(@PathVariable Long id, Authentication a) { return service.content(a.getName(), id); }

    @GetMapping("/notes/{id}/flashcards")
    public List<FlashcardView> flashcards(@PathVariable Long id, Authentication a) { return service.flashcards(a.getName(), id); }

    @GetMapping("/quizzes/{id}")
    public QuizView quiz(@PathVariable Long id, Authentication a) { return service.quiz(a.getName(), id); }

    @PostMapping("/quizzes/{id}/submit")
    public AttemptResult submit(@PathVariable Long id, @Valid @RequestBody SubmitRequest r, Authentication a) {
        return service.submit(a.getName(), id, r);
    }

    @GetMapping("/attempts/{id}")
    public AttemptResult attempt(@PathVariable Long id, Authentication a) { return service.attempt(a.getName(), id); }

    @GetMapping("/blindspots")
    public List<AttemptResult.Item> blindSpots(Authentication a) { return service.blindSpots(a.getName()); }
}