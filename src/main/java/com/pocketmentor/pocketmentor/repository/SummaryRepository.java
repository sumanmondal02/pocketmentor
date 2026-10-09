package com.pocketmentor.pocketmentor.repository;
import com.pocketmentor.pocketmentor.model.Summary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface SummaryRepository extends JpaRepository<Summary, Long> {
    Optional<Summary> findByNoteId(Long noteId);
    void deleteByNoteId(Long noteId);
}