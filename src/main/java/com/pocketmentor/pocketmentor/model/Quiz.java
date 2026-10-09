// Quiz.java
package com.pocketmentor.pocketmentor.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "quizzes")
@Getter @Setter @NoArgsConstructor
public class Quiz {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "note_id") private Note note;
    private LocalDateTime createdAt = LocalDateTime.now();
}