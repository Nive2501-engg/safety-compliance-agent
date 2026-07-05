package com.safetyagent.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
@Data
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    private String reporterName;

    private String reporterPhone;
    private String reporterEmail;

    @Column(nullable = false)
    private String companyName;

    @ManyToOne
    @JoinColumn(name = "machine_id")
    private Machine machine;

    private String severity;
    private String category;

    @Column(columnDefinition = "TEXT")
    private String recommendedAction;

    private LocalDateTime reportedAt = LocalDateTime.now();
}