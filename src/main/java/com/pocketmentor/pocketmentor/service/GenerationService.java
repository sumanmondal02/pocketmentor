package com.pocketmentor.pocketmentor.service;

import com.pocketmentor.pocketmentor.dto.GeneratedContent;
import com.pocketmentor.pocketmentor.model.*;
import com.pocketmentor.pocketmentor.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.ExecutorService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerationService {
    private final TransactionTemplate tx;
    private final NoteRepository notes;
    private final UserRepository users;
    private final TopicRepository topics;
    private final SummaryRepository summaries;
    private final FlashcardRepository flashcards;
    private final QuizRepository quizzes;
    private final QuestionRepository questions;
    private final AiService ai;
    private final RateLimiter limiter;
    private final JobRegistry jobs;
    private final ExecutorService genExecutor;

    // public Map<String, Object> generate(String email, Long noteId) {
    //     Long uid = users.findByEmail(email)
    //             .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)).getId();
    //     Note note = notes.findByIdAndUserId(noteId, uid)
    //             .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
    //     limiter.check("gen:" + uid, 7, 3_600_000L);
    //     GeneratedContent g = ai.generate(note.getRawText()); // slow call, outside the DB transaction
    //     return tx.execute(status -> persist(note, g));
    // }

    public Map<String, Object> start(String email, Long noteId) {
        Long uid = users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)).getId();
        Note note = notes.findByIdAndUserId(noteId, uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
        limiter.check("gen:" + uid, 10, 3_600_000L);
        if (!jobs.tryStart(noteId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already generating for this note");
        genExecutor.execute(() -> run(note));
        return Map.of("noteId", noteId, "status", "PROCESSING");
    }

    private void run(Note note) {
        try {
            GeneratedContent g = ai.generate(note.getRawText());
            tx.execute(s -> persist(note, g));
            jobs.done(note.getId());
        } catch (Exception e) {
            log.warn("Generation failed for note {}: {}", note.getId(), e.getMessage());
            String msg = e instanceof ResponseStatusException r && r.getReason() != null
                    ? r.getReason() : "Generation failed. Please try again.";
            jobs.fail(note.getId(), msg);
        }
    }

    public Map<String, Object> status(String email, Long noteId) {
        Long uid = users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)).getId();
        notes.findByIdAndUserId(noteId, uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
        JobRegistry.Job j = jobs.get(noteId);
        String st = j != null ? j.status() : (summaries.existsByNoteId(noteId) ? "COMPLETED" : "IDLE");
        return Map.of("status", st, "error", j != null && j.error() != null ? j.error() : "");
    }

    private Map<String, Object> persist(Note note, GeneratedContent g) {
        Long nid = note.getId();

        // delete old data (order matters)
        flashcards.deleteByNoteId(nid);
        questions.detachTopics(nid);
        summaries.deleteByNoteId(nid);
        topics.deleteByNoteId(nid);

        Summary s = new Summary();
        s.setNote(note);
        s.setContent(g.summary());
        summaries.save(s);

        Map<String, Topic> byName = new HashMap<>();
        for (String name : g.topics()) {
            Topic t = new Topic();
            t.setNote(note);
            t.setName(name);
            byName.put(name.toLowerCase(), topics.save(t));
        }
        for (GeneratedContent.Card c : g.flashcards()) {
            Flashcard f = new Flashcard();
            f.setNote(note);
            f.setQuestion(c.question());
            f.setAnswer(c.answer());
            f.setTopic(c.topic() == null ? null : byName.get(c.topic().toLowerCase()));
            flashcards.save(f);
        }
        Quiz quiz = new Quiz();
        quiz.setNote(note);
        quizzes.save(quiz);
        for (GeneratedContent.Q q : g.questions()) {
            if (q.options() == null || q.options().size() != 4) continue;
            Question e = new Question();
            e.setQuiz(quiz);
            e.setText(q.text());
            e.setOptionA(q.options().get(0));
            e.setOptionB(q.options().get(1));
            e.setOptionC(q.options().get(2));
            e.setOptionD(q.options().get(3));
            e.setCorrectOption(q.correct().trim().substring(0, 1).toUpperCase());
            e.setExplanation(q.explanation());
            e.setTopic(q.topic() == null ? null : byName.get(q.topic().toLowerCase()));
            questions.save(e);
        }
        return Map.of("noteId", nid, "quizId", quiz.getId(),
                "flashcards", g.flashcards().size(), "questions", g.questions().size());
    }
}