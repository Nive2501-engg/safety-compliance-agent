package com.safetyagent.service;

import com.google.gson.Gson;
import com.safetyagent.model.DocumentChunk;
import com.safetyagent.model.SafetyDocument;
import com.safetyagent.repository.DocumentChunkRepository;
import com.safetyagent.repository.SafetyDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class DocumentService {

    @Autowired
    private SafetyDocumentRepository safetyDocumentRepository;

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Autowired
    private EmbeddingService embeddingService;

    private final Gson gson = new Gson();

    public SafetyDocument uploadDocument(String title, String fullText, String companyName) {

        SafetyDocument document = new SafetyDocument();
        document.setTitle(title);
        document.setCompanyName(companyName);
        document.setStatus("PROCESSING");
        document = safetyDocumentRepository.save(document);

        List<String> chunks = splitIntoChunks(fullText);

        for (String chunkText : chunks) {
            if (chunkText.isBlank()) continue;

            List<Double> embedding = embeddingService.generateEmbedding(chunkText);

            DocumentChunk chunk = new DocumentChunk();
            chunk.setDocument(document);
            chunk.setCompanyName(companyName);
            chunk.setContent(chunkText.trim());
            chunk.setEmbedding(gson.toJson(embedding));
            documentChunkRepository.save(chunk);
        }

        document.setStatus("INDEXED");
        return safetyDocumentRepository.save(document);
    }

    private List<String> splitIntoChunks(String text) {
        String[] paragraphs = text.split("\\n\\n+");
        if (paragraphs.length > 1) {
            return Arrays.asList(paragraphs);
        }
        return Arrays.asList(text.split("(?<=\\.)\\s+"));
    }

    public List<SafetyDocument> getDocumentsByCompany(String companyName) {
        return safetyDocumentRepository.findByCompanyName(companyName);
    }
    public void deleteDocument(Long documentId) {
    List<DocumentChunk> chunks = documentChunkRepository.findAll().stream()
            .filter(c -> c.getDocument() != null && c.getDocument().getId().equals(documentId))
            .toList();
    documentChunkRepository.deleteAll(chunks);
    safetyDocumentRepository.deleteById(documentId);
}
}