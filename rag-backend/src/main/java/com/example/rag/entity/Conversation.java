package com.example.rag.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userMessage;
    private String assistantResponse;
    private LocalDateTime createdAt;

    @Column(columnDefinition = "JSON")
    private String context; // JSON array of relevant chunks

    public Conversation(String userMessage, String assistantResponse, String context) {
        this.userMessage = userMessage;
        this.assistantResponse = assistantResponse;
        this.context = context;
        this.createdAt = LocalDateTime.now();
    }
}
