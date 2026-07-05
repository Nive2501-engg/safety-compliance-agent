package com.safetyagent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @GetMapping("/api/ping")
    public String ping() {
        return "Safety Compliance Agent backend is running ✅";
    }
}