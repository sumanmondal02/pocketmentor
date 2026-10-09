package com.pocketmentor.pocketmentor.service;

import com.pocketmentor.pocketmentor.dto.*;
import com.pocketmentor.pocketmentor.model.*;
import com.pocketmentor.pocketmentor.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {
    private static final int MAX_CHARS = 100_000;
    private final NoteRepository notes;
    private final UserRepository users;

    private User user(String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private String clean(String t) {
        String c = t.replaceAll("[ \\t]+", " ").replaceAll("\\n{3,}", "\n\n").trim();
        return c.length() > MAX_CHARS ? c.substring(0, MAX_CHARS) : c;
    }

    private NoteDetail save(User u, String title, String text, Note.SourceType type) {
        Note n = new Note();
        n.setUser(u);
        n.setTitle(title == null || title.isBlank() ? "Untitled note" : title.trim());
        n.setRawText(text);
        n.setSourceType(type);
        notes.save(n);
        return toDetail(n);
    }

    public NoteDetail saveText(String email, NoteTextRequest r) {
        return save(user(email), r.title(), clean(r.text()), Note.SourceType.TEXT);
    }

    public NoteDetail savePdf(String email, MultipartFile file, String title) throws IOException {
        byte[] bytes = file.getBytes();
        boolean isPdf = bytes.length > 4 && bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F';
        if (!isPdf) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF files are allowed");
        String text;
        try (PDDocument doc = Loader.loadPDF(bytes)) {
            text = clean(new PDFTextStripper().getText(doc));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read this PDF");
        }
        if (text.isBlank())
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "No readable text found (scanned PDF?)");
        String name = (title == null || title.isBlank()) ? file.getOriginalFilename() : title;
        return save(user(email), name, text, Note.SourceType.PDF);
    }

    public List<NoteSummary> list(String email) {
        return notes.findByUserIdOrderByCreatedAtDesc(user(email).getId()).stream()
                .map(n -> new NoteSummary(n.getId(), n.getTitle(), n.getSourceType().name(), n.getCreatedAt()))
                .toList();
    }

    public NoteDetail get(String email, Long id) { return toDetail(find(email, id)); }

    public void delete(String email, Long id) { notes.delete(find(email, id)); }

    private Note find(String email, Long id) {
        return notes.findByIdAndUserId(id, user(email).getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
    }

    private NoteDetail toDetail(Note n) {
        return new NoteDetail(n.getId(), n.getTitle(), n.getSourceType().name(), n.getCreatedAt(), n.getRawText());
    }
}