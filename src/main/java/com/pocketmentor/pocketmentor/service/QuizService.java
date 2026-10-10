package com.pocketmentor.pocketmentor.service;

import com.pocketmentor.pocketmentor.dto.*;
import com.pocketmentor.pocketmentor.model.*;
import com.pocketmentor.pocketmentor.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final UserRepository users;
    private final NoteRepository notes;
    private final QuizRepository quizzes;
    private final QuestionRepository questions;
    private final FlashcardRepository flashcards;
    private final SummaryRepository summaries;
    private final TopicRepository topics;
    private final QuizAttemptRepository attempts;
    private final AttemptAnswerRepository answers;

    private User user(String email) {
        return users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
    private ResponseStatusException notFound(String m) { return new ResponseStatusException(HttpStatus.NOT_FOUND, m); }

    private void ownNote(String email, Long noteId) {
        notes.findByIdAndUserId(noteId, user(email).getId()).orElseThrow(() -> notFound("Note not found"));
    }

    @Transactional(readOnly = true)
    public NoteContent content(String email, Long noteId) {
        ownNote(email, noteId);
        String summary = summaries.findByNoteId(noteId).map(Summary::getContent).orElse(null);
        List<String> t = topics.findByNoteId(noteId).stream().map(Topic::getName).toList();
        Long quizId = quizzes.findFirstByNoteIdOrderByIdDesc(noteId).map(Quiz::getId).orElse(null);
        return new NoteContent(summary, t, quizId);
    }

    @Transactional(readOnly = true)
    public List<FlashcardView> flashcards(String email, Long noteId) {
        ownNote(email, noteId);
        return flashcards.findByNoteId(noteId).stream()
            .map(f -> new FlashcardView(f.getId(), f.getQuestion(), f.getAnswer(),
                f.getTopic() == null ? null : f.getTopic().getName()))
            .toList();
    }

    @Transactional(readOnly = true)
    public QuizView quiz(String email, Long quizId) {
        Quiz q = quizzes.findByIdAndNoteUserId(quizId, user(email).getId()).orElseThrow(() -> notFound("Quiz not found"));
        List<QuestionView> qs = questions.findByQuizId(quizId).stream()
            .map(x -> new QuestionView(x.getId(), x.getText(), x.getOptionA(), x.getOptionB(), x.getOptionC(),
                x.getOptionD(), x.getTopic() == null ? null : x.getTopic().getName()))
            .toList();
        return new QuizView(q.getId(), q.getNote().getId(), qs);
    }

    @Transactional
    public AttemptResult submit(String email, Long quizId, SubmitRequest req) {
        User u = user(email);
        Quiz quiz = quizzes.findByIdAndNoteUserId(quizId, u.getId()).orElseThrow(() -> notFound("Quiz not found"));
        Map<Long, Question> byId = new HashMap<>();
        for (Question q : questions.findByQuizId(quizId)) byId.put(q.getId(), q);

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(u);
        attempt.setQuiz(quiz);
        attempts.save(attempt);

        List<AttemptResult.Item> items = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        int score = 0, blind = 0;

        for (SubmitRequest.Item it : req.answers()) {
            Question q = byId.get(it.questionId());
            if (q == null || !seen.add(q.getId()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or duplicate question: " + it.questionId());

            AttemptAnswer.Confidence conf;
            try { conf = AttemptAnswer.Confidence.valueOf(it.confidence().trim().toUpperCase()); }
            catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Confidence must be SURE, NOT_SURE or GUESSING");
            }
            String sel = it.selected().trim().toUpperCase();
            if (!List.of("A", "B", "C", "D").contains(sel))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected option must be A, B, C or D");

            boolean correct = sel.equals(q.getCorrectOption());
            boolean blindSpot = !correct && conf == AttemptAnswer.Confidence.SURE;
            if (correct) score++;
            if (blindSpot) blind++;

            AttemptAnswer a = new AttemptAnswer();
            a.setAttempt(attempt); a.setQuestion(q); a.setSelectedOption(sel);
            a.setConfidence(conf); a.setCorrect(correct); a.setBlindSpot(blindSpot);
            answers.save(a);

            items.add(new AttemptResult.Item(q.getId(), q.getText(), sel, q.getCorrectOption(),
                optionText(q, q.getCorrectOption()), q.getExplanation(), conf.name(), correct, blindSpot,
                q.getTopic() == null ? null : q.getTopic().getName()));
        }
        attempt.setScore(score);
        attempt.setTotal(byId.size());
        attempts.save(attempt);
        return new AttemptResult(attempt.getId(), score, byId.size(), blind, items);
    }

    @Transactional(readOnly = true)
    public AttemptResult attempt(String email, Long attemptId) {
        QuizAttempt at = attempts.findByIdAndUserId(attemptId, user(email).getId()).orElseThrow(() -> notFound("Attempt not found"));
        List<AttemptResult.Item> items = answers.findByAttemptId(attemptId).stream().map(a -> {
            Question q = a.getQuestion();
            return new AttemptResult.Item(q.getId(), q.getText(), a.getSelectedOption(), q.getCorrectOption(),
                optionText(q, q.getCorrectOption()), q.getExplanation(), a.getConfidence().name(), a.isCorrect(),
                a.isBlindSpot(), q.getTopic() == null ? null : q.getTopic().getName());
        }).toList();
        int blind = (int) items.stream().filter(AttemptResult.Item::blindSpot).count();
        return new AttemptResult(at.getId(), at.getScore(), at.getTotal(), blind, items);
    }

    @Transactional(readOnly = true)
    public List<AttemptResult.Item> blindSpots(String email) {
        return answers.findByAttemptUserIdAndBlindSpotTrue(user(email).getId()).stream().map(a -> {
            Question q = a.getQuestion();
            return new AttemptResult.Item(q.getId(), q.getText(), a.getSelectedOption(), q.getCorrectOption(),
                optionText(q, q.getCorrectOption()), q.getExplanation(), a.getConfidence().name(), false, true,
                q.getTopic() == null ? null : q.getTopic().getName());
        }).toList();
    }

    private String optionText(Question q, String letter) {
        return switch (letter) { case "A" -> q.getOptionA(); case "B" -> q.getOptionB();
            case "C" -> q.getOptionC(); default -> q.getOptionD(); };
    }
}