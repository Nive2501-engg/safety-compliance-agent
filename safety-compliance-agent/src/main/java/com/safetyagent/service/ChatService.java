package com.safetyagent.service;

import com.safetyagent.dto.ChatResponse;
import com.safetyagent.model.DocumentChunk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private RetrievalService retrievalService;

    @Autowired
    @Qualifier("geminiProvider")
    private LLMProvider llmProvider;

    public ChatResponse getAnswer(String question, String companyName) {

        List<DocumentChunk> relevantChunks = retrievalService.findRelevantChunks(question, companyName, 3);

        if (relevantChunks.isEmpty()) {
            return new ChatResponse(
                "I don't have any safety documents indexed yet for your company. Please upload documents first.",
                List.of()
            );
        }

        String context = relevantChunks.stream()
                .map(DocumentChunk::getContent)
                .collect(Collectors.joining("\n---\n"));

        String prompt = buildPrompt(context, question);
        String answer = llmProvider.generateResponse(prompt);

        List<String> sources = relevantChunks.stream()
                .map(DocumentChunk::getContent)
                .toList();

        return new ChatResponse(answer, sources);
    }

    private String buildPrompt(String context, String question) {
        return """
            You are a manufacturing safety compliance assistant. Answer the worker's question 
            using ONLY the safety document context provided below. Do not use any outside knowledge.
            
            If the answer is not found in the context, respond exactly with:
            "I don't have that information in the current safety documents. Please check with your supervisor."
            
            Be clear, concise, and prioritize worker safety in your tone.
Respond in the SAME language the worker used to ask the question 
(if they asked in Tamil, answer in Tamil; if in English, answer in English).
            
            --- SAFETY DOCUMENT CONTEXT ---
            %s
            --- END CONTEXT ---
            
            Worker's question: %s
            
            Answer:
            """.formatted(context, question);
    }
}