package com.safetyagent.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com/v1beta/models")
            .build();

    public List<Double> generateEmbedding(String text) {
        Map<String, Object> requestBody = Map.of(
            "content", Map.of("parts", List.of(Map.of("text", text)))
        );

        Map response = webClient.post()
                .uri("/gemini-embedding-001:embedContent?key=" + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        Map embeddingObj = (Map) response.get("embedding");
        return (List<Double>) embeddingObj.get("values");
    }
}