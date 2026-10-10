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
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {
    private static final int MAX_CHARS = 100_000;
    private final NoteRepository notes;
    private final UserRepository users;
    private final AttemptAnswerRepository answers;
    private final QuizAttemptRepository attempts;
    private final QuestionRepository questions;
    private final QuizRepository quizzes;
    private final FlashcardRepository flashcards;
    private final SummaryRepository summaries;
    private final TopicRepository topics;
    private final JobRegistry jobs;

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
        String t = (title == null || title.isBlank()) ? "Untitled note" : title.trim();
        n.setTitle(t.length() > 200 ? t.substring(0, 200) : t);
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
        String fname = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        boolean isPdf = bytes.length > 4 && bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F';
        String text;
        Note.SourceType type;
        if (isPdf) {
            try (PDDocument doc = Loader.loadPDF(bytes)) {
                text = clean(new PDFTextStripper().getText(doc));
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read this PDF");
            }
            type = Note.SourceType.PDF;
        } else if (fname.toLowerCase().endsWith(".txt")) {
            text = clean(new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
            type = Note.SourceType.TEXT;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF or TXT files are allowed");
        }
        if (text.isBlank())
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "No readable text found");
        String name = (title == null || title.isBlank()) ? fname : title;
        return save(user(email), name, text, type);
    }

    public List<NoteSummary> list(String email) {
        return notes.findByUserIdOrderByCreatedAtDesc(user(email).getId()).stream().map(n -> {
            JobRegistry.Job j = jobs.get(n.getId());
            String st = j != null && !"COMPLETED".equals(j.status()) ? j.status()
                    : summaries.existsByNoteId(n.getId()) ? "READY" : "NOT_GENERATED";
            long fc = flashcards.countByNoteId(n.getId());
            long qc = quizzes.findFirstByNoteIdOrderByIdDesc(n.getId())
                    .map(q -> questions.countByQuizId(q.getId())).orElse(0L);
            return new NoteSummary(n.getId(), n.getTitle(), n.getSourceType().name(), n.getCreatedAt(), st, fc, qc);
        }).toList();
    }

    public NoteDetail get(String email, Long id) { return toDetail(find(email, id)); }

    @Transactional
    public void delete(String email, Long id) {
        Note n = find(email, id);
        Long nid = n.getId();
        answers.deleteByNote(nid);
        attempts.deleteByNote(nid);
        questions.deleteByNote(nid);
        quizzes.deleteByNote(nid);
        flashcards.deleteByNoteId(nid);
        summaries.deleteByNoteId(nid);
        topics.deleteByNoteId(nid);
        notes.deleteById(nid);
    }

    private Note find(String email, Long id) {
        return notes.findByIdAndUserId(id, user(email).getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
    }

    private NoteDetail toDetail(Note n) {
        return new NoteDetail(n.getId(), n.getTitle(), n.getSourceType().name(), n.getCreatedAt(), n.getRawText());
    }
}