package com.example.rag.controller;

import com.example.rag.entity.Chunk;
import com.example.rag.entity.Document;
import com.example.rag.repository.ChunkRepository;
import com.example.rag.repository.DocumentRepository;
import com.example.rag.service.EmbeddingService;
import com.example.rag.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Validated
public class UploadController {

    private final PdfService pdfService;
    private final EmbeddingService embeddingService;
    private final DocumentRepository documentRepository;
    private final ChunkRepository chunkRepository;

    @Autowired
    public UploadController(PdfService pdfService, EmbeddingService embeddingService,
                           DocumentRepository documentRepository, ChunkRepository chunkRepository) {
        this.pdfService = pdfService;
        this.embeddingService = embeddingService;
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
    }

    @PostMapping(path = "/upload", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> uploadPdf(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "no file");
            return ResponseEntity.badRequest().body(resp);
        }

        // Extract text
        String text = pdfService.extractText(file.getInputStream());
        List<String> chunks = pdfService.chunkText(text, 1200);

        // Create document record
        Document doc = new Document();
        doc.setName(file.getOriginalFilename());
        doc.setPath("/uploads/" + file.getOriginalFilename());
        doc.setUploadedAt(LocalDateTime.now());
        Document savedDoc = documentRepository.save(doc);

        // Generate embeddings for each chunk and save
        List<Chunk> savedChunks = new ArrayList<>();
        for (String chunk : chunks) {
            try {
                String embeddingJson = embeddingService.embedAsJson(chunk);
                Chunk c = new Chunk(savedDoc, chunk, embeddingJson);
                Chunk saved = chunkRepository.save(c);
                savedChunks.add(saved);
            } catch (Exception e) {
                // Log but continue
                System.err.println("Failed to embed chunk: " + e.getMessage());
            }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "uploaded");
        resp.put("documentId", savedDoc.getId());
        resp.put("totalChunks", chunks.size());
        resp.put("embeddedChunks", savedChunks.size());
        return ResponseEntity.ok(resp);
    }
}
