package com.pocketmentor.pocketmentor.repository;
import com.pocketmentor.pocketmentor.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUserIdOrderByTakenAtDesc(Long userId);
    Optional<QuizAttempt> findByIdAndUserId(Long id, Long userId);
}