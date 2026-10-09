package com.pocketmentor.pocketmentor.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
@Getter @Setter @NoArgsConstructor
public class Note {
    public enum SourceType { PDF, TEXT }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(nullable = false) private String title;
    @Column(nullable = false, columnDefinition = "LONGTEXT") private String rawText;
    @Enumerated(EnumType.STRING) private SourceType sourceType;
    private LocalDateTime createdAt = LocalDateTime.now();
}