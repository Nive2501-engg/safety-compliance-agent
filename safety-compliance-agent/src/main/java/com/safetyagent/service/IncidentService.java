package com.safetyagent.service;

import com.google.gson.JsonObject;
import com.safetyagent.model.Incident;
import com.safetyagent.repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncidentService {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private IncidentClassificationService classificationService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private EmailService emailService;

    public Incident createIncident(Incident incident) {

        JsonObject classification = classificationService.classifyIncident(incident.getDescription());

        incident.setSeverity(classification.get("severity").getAsString());
        incident.setCategory(classification.get("category").getAsString());
        incident.setRecommendedAction(classification.get("recommendedAction").getAsString());

        Incident saved = incidentRepository.save(incident);

        if ("high".equalsIgnoreCase(saved.getSeverity())) {
            smsService.sendHighSeverityAlert(saved.getDescription(), saved.getReporterPhone());
            emailService.sendAlertEmail(
                saved.getReporterEmail(),
                "\"Safety Incident Alert - High Severity\",",
                "Machine: " + (saved.getMachine() != null ? saved.getMachine().getName() : "N/A") + "\n" +
                "Description: " + saved.getDescription() + "\n" +
                "Reported by: " + saved.getReporterName()
            );
        }

        return saved;
    }

    public List<Incident> getIncidentsByCompany(String companyName) {
        return incidentRepository.findByCompanyName(companyName);
    }

    public void deleteIncident(Long id) {
        incidentRepository.deleteById(id);
    }
}