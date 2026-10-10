package com.pocketmentor.pocketmentor.repository;

import com.pocketmentor.pocketmentor.model.Summary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface SummaryRepository extends JpaRepository<Summary, Long> {
    Optional<Summary> findByNoteId(Long noteId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Summary s where s.note.id = :nid")
    void deleteByNoteId(@Param("nid") Long nid);
}