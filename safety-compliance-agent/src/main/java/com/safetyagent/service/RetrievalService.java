package com.safetyagent.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.safetyagent.model.DocumentChunk;
import com.safetyagent.repository.DocumentChunkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;

@Service
public class RetrievalService {

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Autowired
    private EmbeddingService embeddingService;

    private final Gson gson = new Gson();

    public List<DocumentChunk> findRelevantChunks(String question, String companyName, int topK) {
        List<Double> questionEmbedding = embeddingService.generateEmbedding(question);
        List<DocumentChunk> companyChunks = documentChunkRepository.findByCompanyName(companyName);

        return companyChunks.stream()
                .sorted(Comparator.comparingDouble(
                        chunk -> -cosineSimilarity(questionEmbedding, parseEmbedding(chunk.getEmbedding()))
                ))
                .limit(topK)
                .toList();
    }

    private List<Double> parseEmbedding(String json) {
        Type listType = new TypeToken<List<Double>>() {}.getType();
        return gson.fromJson(json, listType);
    }

    private double cosineSimilarity(List<Double> a, List<Double> b) {
        double dotProduct = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < a.size(); i++) {
            dotProduct += a.get(i) * b.get(i);
            normA += Math.pow(a.get(i), 2);
            normB += Math.pow(b.get(i), 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}