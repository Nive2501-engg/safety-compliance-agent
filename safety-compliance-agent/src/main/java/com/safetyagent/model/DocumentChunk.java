package com.safetyagent.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "document_chunks")
@Data
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private SafetyDocument document;

    @Column(nullable = false)
    private String companyName;   // 🆕 new field (denormalized for fast filtering)

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "LONGTEXT")
    private String embedding;
}