package com.pocketmentor.pocketmentor.repository;
import com.pocketmentor.pocketmentor.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByNoteId(Long noteId);
    Optional<Quiz> findFirstByNoteIdOrderByIdDesc(Long noteId);
    Optional<Quiz> findByIdAndNoteUserId(Long id, Long userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Quiz z where z.note.id = :nid")
    void deleteByNote(@Param("nid") Long nid);
}