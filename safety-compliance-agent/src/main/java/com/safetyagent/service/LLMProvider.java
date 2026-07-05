package com.safetyagent.service;

public interface LLMProvider {
    String generateResponse(String prompt);
}