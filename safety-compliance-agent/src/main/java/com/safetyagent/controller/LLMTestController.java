package com.safetyagent.controller;

import com.safetyagent.service.LLMProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class LLMTestController {

    private final LLMProvider geminiProvider;
    private final LLMProvider anthropicProvider;

    public LLMTestController(
            @Qualifier("geminiProvider") LLMProvider geminiProvider,
            @Qualifier("anthropicProvider") LLMProvider anthropicProvider) {
        this.geminiProvider = geminiProvider;
        this.anthropicProvider = anthropicProvider;
    }

    @GetMapping("/gemini")
    public String testGemini() {
        return geminiProvider.generateResponse("Say hello and confirm you are working, in one sentence.");
    }

    @GetMapping("/anthropic")
    public String testAnthropic() {
        return anthropicProvider.generateResponse("Say hello and confirm you are working, in one sentence.");
    }
}