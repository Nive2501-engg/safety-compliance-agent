package com.safetyagent.controller;

import com.safetyagent.dto.DocumentUploadRequest;
import com.safetyagent.model.SafetyDocument;
import com.safetyagent.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping("/upload")
    public SafetyDocument uploadDocument(@RequestBody DocumentUploadRequest request) {
        return documentService.uploadDocument(request.getTitle(), request.getContent(), request.getCompanyName());
    }

    @GetMapping
    public List<SafetyDocument> getDocuments(@RequestParam String companyName) {
        return documentService.getDocumentsByCompany(companyName);
    }
     @DeleteMapping("/{id}")
    public void deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
    }
}