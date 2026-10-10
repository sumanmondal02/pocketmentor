package com.pocketmentor.pocketmentor.repository;

import com.pocketmentor.pocketmentor.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuizId(Long quizId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Question q set q.topic = null where q.quiz.id in (select z.id from Quiz z where z.note.id = :nid)")
    void detachTopics(@Param("nid") Long nid);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Question q where q.quiz.id in (select z.id from Quiz z where z.note.id = :nid)")
    void deleteByNote(@Param("nid") Long nid);
}