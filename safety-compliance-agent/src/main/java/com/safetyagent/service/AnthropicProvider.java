package com.safetyagent.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service("anthropicProvider")
public class AnthropicProvider implements LLMProvider {

    @Value("${anthropic.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.anthropic.com/v1")
            .build();

    @Override
    public String generateResponse(String prompt) {
        Map<String, Object> requestBody = Map.of(
            "model", "claude-sonnet-4-6",
            "max_tokens", 1024,
            "messages", List.of(
                Map.of("role", "user", "content", prompt)
            )
        );

        Map response = webClient.post()
                .uri("/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        try {
            List content = (List) response.get("content");
            Map firstBlock = (Map) content.get(0);
            return (String) firstBlock.get("text");
        } catch (Exception e) {
            return "Error parsing Anthropic response: " + e.getMessage();
        }
    }
}