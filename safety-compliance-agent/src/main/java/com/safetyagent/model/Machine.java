package com.safetyagent.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "machines")
@Data
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String companyName;   // 🆕 new field

    private String type;
    private String location;

    @Column(name = "safety_notes", columnDefinition = "TEXT")
    private String safetyNotes;
}