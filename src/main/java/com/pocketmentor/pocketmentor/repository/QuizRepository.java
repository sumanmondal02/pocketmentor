package com.pocketmentor.pocketmentor.repository;
import com.pocketmentor.pocketmentor.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByNoteId(Long noteId);
}