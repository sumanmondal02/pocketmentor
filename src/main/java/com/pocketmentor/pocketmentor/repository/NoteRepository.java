package com.pocketmentor.pocketmentor.repository;

import com.pocketmentor.pocketmentor.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Note> findByIdAndUserId(Long id, Long userId);
}