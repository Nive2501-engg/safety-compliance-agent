package com.safetyagent.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String question;
    private String companyName;
}