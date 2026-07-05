package com.safetyagent.service;

import org.springframework.stereotype.Service;

@Service
public class SmsService {

    public void sendHighSeverityAlert(String incidentDescription, String phoneNumber) {
        System.out.println("=================================");
        System.out.println("🚨 SMS ALERT (simulated) 🚨");
        System.out.println("To: " + (phoneNumber != null ? phoneNumber : "Unknown"));
        System.out.println("Message: HIGH SEVERITY incident reported: " + incidentDescription);
        System.out.println("=================================");
    }
}