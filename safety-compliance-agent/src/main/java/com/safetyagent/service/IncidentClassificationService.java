package com.safetyagent.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class IncidentClassificationService {

    @Autowired
    @Qualifier("geminiProvider")
    private LLMProvider llmProvider;

    private final Gson gson = new Gson();

    public JsonObject classifyIncident(String description) {
        String prompt = buildClassificationPrompt(description);
        String rawResponse = llmProvider.generateResponse(prompt);

        // Clean response in case model wraps it in ```json ... ```
        String cleaned = rawResponse
                .replace("```json", "")
                .replace("```", "")
                .trim();

        try {
            return gson.fromJson(cleaned, JsonObject.class);
        } catch (Exception e) {
            // Fallback if parsing fails
            JsonObject fallback = new JsonObject();
            fallback.addProperty("severity", "medium");
            fallback.addProperty("category", "unclassified");
            fallback.addProperty("recommendedAction", "Manual review required.");
            return fallback;
        }
    }

    private String buildClassificationPrompt(String description) {
        return """
            You are a manufacturing safety incident classifier. Analyze the incident description 
            below and classify it.
            
            Respond with ONLY a valid JSON object, no explanation, no markdown, in this exact format:
            {
              "severity": "low" | "medium" | "high",
              "category": "fall hazard" | "electrical" | "machine malfunction" | "chemical exposure" | "fire hazard" | "ergonomic" | "other",
              "recommendedAction": "a short, one-sentence recommended action"
            }
            
            Examples:
            Incident: "Worker slipped on wet floor near packing area"
            Response: {"severity": "medium", "category": "fall hazard", "recommendedAction": "Place warning signs and clean spill immediately."}
            
            Incident: "Electrical panel sparked while technician was working nearby"
            Response: {"severity": "high", "category": "electrical", "recommendedAction": "Shut down power immediately and call certified electrician."}
            
            Now classify this incident:
            Incident: "%s"
            Response:
            """.formatted(description);
    }
}