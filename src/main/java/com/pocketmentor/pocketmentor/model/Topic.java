package com.pocketmentor.pocketmentor.model;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "topics")
@Getter @Setter @NoArgsConstructor
public class Topic {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "note_id") private Note note;
    @Column(nullable = false) private String name;
}