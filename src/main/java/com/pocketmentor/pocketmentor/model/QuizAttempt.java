package com.pocketmentor.pocketmentor.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "quiz_attempts")
@Getter @Setter @NoArgsConstructor
public class QuizAttempt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "quiz_id") private Quiz quiz;
    private int score;
    private int total;
    private LocalDateTime takenAt = LocalDateTime.now();
}