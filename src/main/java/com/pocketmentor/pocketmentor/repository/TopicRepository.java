package com.pocketmentor.pocketmentor.repository;

import com.pocketmentor.pocketmentor.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findByNoteId(Long noteId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Topic t where t.note.id = :nid")
    void deleteByNoteId(@Param("nid") Long nid);
}