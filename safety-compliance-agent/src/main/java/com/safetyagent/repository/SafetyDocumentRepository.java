package com.safetyagent.repository;

import com.safetyagent.model.SafetyDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SafetyDocumentRepository extends JpaRepository<SafetyDocument, Long> {
    List<SafetyDocument> findByCompanyName(String companyName);
}