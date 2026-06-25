package com.example.rag.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class ChatService {

    @Value("${openai.api-key:}")
    private String openaiApiKey;

    @Value("${openai.gpt-model:gpt-4}")
    private String gptModel;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public String chat(String userMessage, List<String> contextChunks) throws IOException, InterruptedException {
        if (openaiApiKey == null || openaiApiKey.isBlank()) {
            throw new IllegalArgumentException("OpenAI API key not configured");
        }

        // Build context from chunks
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("Context:\n");
        for (String chunk : contextChunks) {
            contextBuilder.append("- ").append(chunk).append("\n");
        }

        // System prompt
        String systemPrompt = "You are a helpful assistant. Answer the user's question based on the provided context. "
                + "If the context doesn't contain relevant information, say so.";

        // Build request
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", gptModel);
        requestBody.addProperty("temperature", 0.7);

        JsonArray messages = new JsonArray();

        JsonObject sysMsg = new JsonObject();
        sysMsg.addProperty("role", "system");
        sysMsg.addProperty("content", systemPrompt);
        messages.add(sysMsg);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", contextBuilder.toString() + "\n\nUser question: " + userMessage);
        messages.add(userMsg);

        requestBody.add("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Authorization", "Bearer " + openaiApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("GPT-4 API error: " + response.body());
        }

        JsonObject respObj = gson.fromJson(response.body(), JsonObject.class);
        JsonArray choices = respObj.getAsJsonArray("choices");
        JsonObject choice = choices.get(0).getAsJsonObject();
        JsonObject message = choice.getAsJsonObject("message");
        return message.get("content").getAsString();
    }
}
