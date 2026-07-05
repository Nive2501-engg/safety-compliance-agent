package com.safetyagent.controller;

import com.safetyagent.model.Incident;
import com.safetyagent.service.IncidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    @Autowired
    private IncidentService incidentService;

    @PostMapping
    public Incident createIncident(@RequestBody Incident incident) {
        return incidentService.createIncident(incident);
    }

    @GetMapping
    public List<Incident> getIncidents(@RequestParam String companyName) {
        return incidentService.getIncidentsByCompany(companyName);
    }

@DeleteMapping("/{id}")
    public void deleteIncident(@PathVariable Long id) {
        incidentService.deleteIncident(id);
    }
}