package com.example.rag.controller;

import com.example.rag.entity.Chunk;
import com.example.rag.entity.Conversation;
import com.example.rag.repository.ConversationRepository;
import com.example.rag.service.ChatService;
import com.example.rag.service.SimilaritySearchService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final SimilaritySearchService similaritySearchService;
    private final ChatService chatService;
    private final ConversationRepository conversationRepository;
    private final Gson gson = new Gson();

    @Autowired
    public ChatController(SimilaritySearchService similaritySearchService, ChatService chatService,
                         ConversationRepository conversationRepository) {
        this.similaritySearchService = similaritySearchService;
        this.chatService = chatService;
        this.conversationRepository = conversationRepository;
    }

    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> query(@RequestBody Map<String, String> request) throws Exception {
        String userMessage = request.get("message");
        int topK = Integer.parseInt(request.getOrDefault("topK", "5"));

        // Search for relevant chunks
        List<Chunk> contextChunks = similaritySearchService.searchSimilar(userMessage, topK);
        List<String> contextTexts = contextChunks.stream().map(Chunk::getText).toList();

        // Get GPT-4 response
        String gptResponse = chatService.chat(userMessage, contextTexts);

        // Save conversation
        String contextJson = gson.toJson(contextTexts);
        Conversation conv = new Conversation(userMessage, gptResponse, contextJson);
        Conversation saved = conversationRepository.save(conv);

        Map<String, Object> resp = new HashMap<>();
        resp.put("conversationId", saved.getId());
        resp.put("userMessage", userMessage);
        resp.put("response", gptResponse);
        resp.put("sourceCount", contextChunks.size());
        resp.put("sources", contextTexts);

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/history")
    public ResponseEntity<List<Conversation>> getHistory() {
        List<Conversation> history = conversationRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(history);
    }

    @GetMapping("/conversation/{id}")
    public ResponseEntity<Conversation> getConversation(@PathVariable Long id) {
        return conversationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
