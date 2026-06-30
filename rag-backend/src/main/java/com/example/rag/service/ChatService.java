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

    @Value("${ollama.api-url:http://localhost:11434}")
    private String ollamaApiUrl;

    @Value("${ollama.api-key:}")
    private String ollamaApiKey;

    @Value("${ollama.gpt-model:llama2}")
    private String gptModel;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public String chat(String userMessage, List<String> contextChunks) throws IOException, InterruptedException {

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

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(ollamaApiUrl + "/v1/chat/completions"))
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
        if (respObj.has("choices")) {
            JsonArray choices = respObj.getAsJsonArray("choices");
            JsonObject choice = choices.get(0).getAsJsonObject();
            JsonObject message = choice.getAsJsonObject("message");
            if (message != null && message.has("content")) {
                return message.get("content").getAsString();
            }
        }

        if (respObj.has("output")) {
            return respObj.get("output").getAsString();
        }

        throw new RuntimeException("Unable to parse Ollama response: " + response.body());
    }
}
