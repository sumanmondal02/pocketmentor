package com.pocketmentor.pocketmentor.model;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "flashcards")
@Getter @Setter @NoArgsConstructor
public class Flashcard {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "note_id") private Note note;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "topic_id") private Topic topic;
    @Column(nullable = false, columnDefinition = "TEXT") private String question;
    @Column(nullable = false, columnDefinition = "TEXT") private String answer;
}