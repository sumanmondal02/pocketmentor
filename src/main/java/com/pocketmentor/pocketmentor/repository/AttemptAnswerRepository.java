package com.pocketmentor.pocketmentor.repository;

import com.pocketmentor.pocketmentor.model.AttemptAnswer;
import com.pocketmentor.pocketmentor.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AttemptAnswerRepository extends JpaRepository<AttemptAnswer, Long> {
    List<AttemptAnswer> findByAttemptId(Long attemptId);
    List<AttemptAnswer> findByAttemptUserIdAndBlindSpotTrue(Long userId);

    @Query("""
        select t.name, count(a), sum(case when a.correct = true then 1 else 0 end),
               sum(case when a.blindSpot = true then 1 else 0 end)
        from AttemptAnswer a join a.question q join q.topic t
        where a.attempt.user.id = :uid group by t.name""")
    List<Object[]> topicStats(@Param("uid") Long uid);

    @Query("""
        select distinct q from AttemptAnswer a join a.question q
        where a.attempt.user.id = :uid and q.quiz.note.id = :nid and a.correct = false""")
    List<Question> weakQuestions(@Param("uid") Long uid, @Param("nid") Long nid);
}