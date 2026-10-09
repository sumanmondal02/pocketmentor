package com.pocketmentor.pocketmentor.repository;
import com.pocketmentor.pocketmentor.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findByNoteId(Long noteId);
    void deleteByNoteId(Long noteId);
}