package com.pocketmentor.pocketmentor.model;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "attempt_answers")
@Getter @Setter @NoArgsConstructor
public class AttemptAnswer {
    public enum Confidence { SURE, NOT_SURE, GUESSING }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "attempt_id") private QuizAttempt attempt;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "question_id") private Question question;
    @Column(length = 1) private String selectedOption;
    @Enumerated(EnumType.STRING) private Confidence confidence;
    private boolean correct;
    private boolean blindSpot;
}