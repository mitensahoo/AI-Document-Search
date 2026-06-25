package com.example.rag.service;

import com.example.rag.entity.Chunk;
import com.example.rag.repository.ChunkRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SimilaritySearchService {

    private final ChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;
    private final Gson gson = new Gson();
    private static final Type LIST_DOUBLE_TYPE = new TypeToken<List<Double>>(){}.getType();

    @Autowired
    public SimilaritySearchService(ChunkRepository chunkRepository, EmbeddingService embeddingService) {
        this.chunkRepository = chunkRepository;
        this.embeddingService = embeddingService;
    }

    public List<Chunk> searchSimilar(String query, int topK) throws IOException, InterruptedException {
        // Get embedding for query
        List<Double> queryEmbedding = embeddingService.embed(query);

        // Get all chunks and compute similarity
        List<Chunk> allChunks = chunkRepository.findAll();
        List<ChunkSimilarity> results = new ArrayList<>();

        for (Chunk chunk : allChunks) {
            if (chunk.getEmbedding() == null) continue;

            List<Double> chunkEmbedding = gson.fromJson(chunk.getEmbedding(), LIST_DOUBLE_TYPE);
            double similarity = cosineSimilarity(queryEmbedding, chunkEmbedding);
            results.add(new ChunkSimilarity(chunk, similarity));
        }

        // Sort by similarity descending and take top K
        return results.stream()
                .sorted(Comparator.comparingDouble(ChunkSimilarity::getSimilarity).reversed())
                .limit(topK)
                .map(ChunkSimilarity::getChunk)
                .toList();
    }

    private double cosineSimilarity(List<Double> v1, List<Double> v2) {
        if (v1.size() != v2.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < v1.size(); i++) {
            dotProduct += v1.get(i) * v2.get(i);
            norm1 += v1.get(i) * v1.get(i);
            norm2 += v2.get(i) * v2.get(i);
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private static class ChunkSimilarity {
        private final Chunk chunk;
        private final double similarity;

        ChunkSimilarity(Chunk chunk, double similarity) {
            this.chunk = chunk;
            this.similarity = similarity;
        }

        Chunk getChunk() {
            return chunk;
        }

        double getSimilarity() {
            return similarity;
        }
    }
}

