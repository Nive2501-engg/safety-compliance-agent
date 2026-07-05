package com.safetyagent.dto;

import lombok.Data;

@Data
public class DocumentUploadRequest {
    private String title;
    private String content;
    private String companyName;
}