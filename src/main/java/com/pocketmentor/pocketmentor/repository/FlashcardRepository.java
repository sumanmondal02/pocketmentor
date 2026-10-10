package com.pocketmentor.pocketmentor.repository;

import com.pocketmentor.pocketmentor.model.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    List<Flashcard> findByNoteId(Long noteId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Flashcard f where f.note.id = :nid")
    void deleteByNoteId(@Param("nid") Long nid);
    long countByNoteId(Long noteId);
}