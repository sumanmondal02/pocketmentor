package com.pocketmentor.pocketmentor.model;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "summaries")
@Getter @Setter @NoArgsConstructor
public class Summary {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "note_id", unique = true) private Note note;
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
}