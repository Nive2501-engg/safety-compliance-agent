package com.safetyagent.repository;

import com.safetyagent.model.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    List<DocumentChunk> findByCompanyName(String companyName);
}