package com.pocketmentor.pocketmentor.model;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "questions")
@Getter @Setter @NoArgsConstructor
public class Question {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "quiz_id") private Quiz quiz;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "topic_id") private Topic topic;
    @Column(nullable = false, columnDefinition = "TEXT") private String text;
    @Column(nullable = false) private String optionA;
    @Column(nullable = false) private String optionB;
    @Column(nullable = false) private String optionC;
    @Column(nullable = false) private String optionD;
    @Column(nullable = false, length = 1) private String correctOption; // A-D
    @Column(columnDefinition = "TEXT") private String explanation;
}