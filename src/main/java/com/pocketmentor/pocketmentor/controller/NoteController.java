package com.pocketmentor.pocketmentor.controller;

import com.pocketmentor.pocketmentor.dto.*;
import com.pocketmentor.pocketmentor.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {
    private final NoteService service;
    private final com.pocketmentor.pocketmentor.service.GenerationService generation;

    @PostMapping("/{id}/generate")
    public java.util.Map<String, Object> generate(@PathVariable Long id, Authentication auth) {
        return generation.generate(auth.getName(), id);
    }

    @PostMapping("/text")
    public NoteDetail text(@Valid @RequestBody NoteTextRequest r, Authentication auth) {
        return service.saveText(auth.getName(), r);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public NoteDetail upload(@RequestParam("file") MultipartFile file,
                             @RequestParam(required = false) String title,
                             Authentication auth) throws IOException {
        return service.savePdf(auth.getName(), file, title);
    }

    @GetMapping
    public List<NoteSummary> list(Authentication auth) { return service.list(auth.getName()); }

    @GetMapping("/{id}")
    public NoteDetail get(@PathVariable Long id, Authentication auth) { return service.get(auth.getName(), id); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) { service.delete(auth.getName(), id); }
}