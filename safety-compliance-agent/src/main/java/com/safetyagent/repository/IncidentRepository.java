package com.safetyagent.repository;

import com.safetyagent.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByCompanyName(String companyName);
}