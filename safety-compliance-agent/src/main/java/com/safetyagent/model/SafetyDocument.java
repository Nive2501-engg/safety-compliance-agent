package com.safetyagent.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "safety_documents")
@Data
public class SafetyDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String companyName;   // 🆕 new field

    private String status = "PROCESSING";

    private LocalDateTime uploadedAt = LocalDateTime.now();
}