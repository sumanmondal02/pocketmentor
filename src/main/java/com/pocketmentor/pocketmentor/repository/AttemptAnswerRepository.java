package com.pocketmentor.pocketmentor.repository;
import com.pocketmentor.pocketmentor.model.AttemptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttemptAnswerRepository extends JpaRepository<AttemptAnswer, Long> {
    List<AttemptAnswer> findByAttemptId(Long attemptId);
    List<AttemptAnswer> findByAttemptUserIdAndBlindSpotTrue(Long userId);
}