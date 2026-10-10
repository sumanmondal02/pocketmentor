package com.pocketmentor.pocketmentor.repository;
import com.pocketmentor.pocketmentor.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUserIdOrderByTakenAtDesc(Long userId);
    Optional<QuizAttempt> findByIdAndUserId(Long id, Long userId);
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from QuizAttempt t where t.quiz.id in (select z.id from Quiz z where z.note.id = :nid)")
    void deleteByNote(@Param("nid") Long nid);
}