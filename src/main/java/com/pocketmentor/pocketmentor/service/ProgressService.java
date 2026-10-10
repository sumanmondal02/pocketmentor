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
public class ProgressService {
    private final UserRepository users;
    private final NoteRepository notes;
    private final QuizRepository quizzes;
    private final QuestionRepository questions;
    private final QuizAttemptRepository attempts;
    private final AttemptAnswerRepository answers;

    private User user(String email) {
        return users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    static String mastery(int accuracy, long blindSpots) {
        if (accuracy < 50) return "BEGINNER";
        if (accuracy > 80 && blindSpots == 0) return "MASTERED";
        return "LEARNING";
    }

    @Transactional(readOnly = true)
    public ProgressView progress(String email) {
        Long uid = user(email).getId();
        List<ProgressView.TopicStat> topics = new ArrayList<>();
        long all = 0, right = 0, blind = 0;
        for (Object[] r : answers.topicStats(uid)) {
            long n = ((Number) r[1]).longValue(), c = ((Number) r[2]).longValue(), b = ((Number) r[3]).longValue();
            int acc = n == 0 ? 0 : (int) Math.round(100.0 * c / n);
            topics.add(new ProgressView.TopicStat((String) r[0], n, c, b, acc, mastery(acc, b)));
            all += n; right += c; blind += b;
        }
        topics.sort(Comparator.comparingInt(ProgressView.TopicStat::accuracy));   // weakest first
        List<ProgressView.HistoryItem> history = attempts.findByUserIdOrderByTakenAtDesc(uid).stream()
            .map(a -> new ProgressView.HistoryItem(a.getId(), a.getQuiz().getId(),
                a.getQuiz().getNote().getTitle(), a.getScore(), a.getTotal(), a.getTakenAt()))
            .toList();
        int overall = all == 0 ? 0 : (int) Math.round(100.0 * right / all);
        return new ProgressView((int) all, overall, (int) blind, topics, history);
    }

    @Transactional
    public QuizView retakeWeak(String email, Long noteId) {
        Long uid = user(email).getId();
        Note note = notes.findByIdAndUserId(noteId, uid)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
        List<Question> weak = answers.weakQuestions(uid, noteId);
        if (weak.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No weak questions yet. Take a quiz first.");
        Collections.shuffle(weak);
        if (weak.size() > 10) weak = weak.subList(0, 10);

        Quiz quiz = new Quiz();
        quiz.setNote(note);
        quizzes.save(quiz);
        List<QuestionView> views = new ArrayList<>();
        for (Question o : weak) {
            Question c = new Question();
            c.setQuiz(quiz); c.setTopic(o.getTopic()); c.setText(o.getText());
            c.setOptionA(o.getOptionA()); c.setOptionB(o.getOptionB());
            c.setOptionC(o.getOptionC()); c.setOptionD(o.getOptionD());
            c.setCorrectOption(o.getCorrectOption()); c.setExplanation(o.getExplanation());
            questions.save(c);
            views.add(new QuestionView(c.getId(), c.getText(), c.getOptionA(), c.getOptionB(),
                c.getOptionC(), c.getOptionD(), c.getTopic() == null ? null : c.getTopic().getName()));
        }
        return new QuizView(quiz.getId(), noteId, views);
    }
}