package com.example.rag.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chunks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    private String text;

    @Column(columnDefinition = "JSON")
    private String embedding; // stored as JSON array string

    public Chunk(Document document, String text, String embedding) {
        this.document = document;
        this.text = text;
        this.embedding = embedding;
    }
}
