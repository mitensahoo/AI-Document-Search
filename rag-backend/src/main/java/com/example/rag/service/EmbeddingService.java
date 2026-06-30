package com.example.rag.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmbeddingService {

    @Value("${ollama.api-url:http://localhost:11434}")
    private String ollamaApiUrl;

    @Value("${ollama.api-key:}")
    private String ollamaApiKey;

    @Value("${ollama.embedding-model:llama2}")
    private String embeddingModel;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public List<Double> embed(String text) throws IOException, InterruptedException {
        if (embeddingModel == null || embeddingModel.isBlank()) {
            throw new IllegalArgumentException("Ollama embedding model not configured");
        }

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", embeddingModel);
        JsonArray inputArray = new JsonArray();
        inputArray.add(text);
        requestBody.add("input", inputArray);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(ollamaApiUrl + "/v1/embeddings"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()));

        if (ollamaApiKey != null && !ollamaApiKey.isBlank()) {
            requestBuilder.header("Authorization", "Bearer " + ollamaApiKey);
        }

        HttpRequest request = requestBuilder.build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Ollama API error: " + response.body());
        }

        JsonObject respObj = gson.fromJson(response.body(), JsonObject.class);
        JsonArray data = respObj.getAsJsonArray("data");
        JsonObject embedding = data.get(0).getAsJsonObject();
        JsonArray embeddingArray = embedding.getAsJsonArray("embedding");

        List<Double> result = new ArrayList<>();
        for (int i = 0; i < embeddingArray.size(); i++) {
            result.add(embeddingArray.get(i).getAsDouble());
        }
        return result;
    }

    public String embedAsJson(String text) throws IOException, InterruptedException {
        List<Double> embedding = embed(text);
        return gson.toJson(embedding);
    }
}
