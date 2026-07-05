package com.safetyagent.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChatResponse {
    private String answer;
    private List<String> sources;

    public ChatResponse(String answer, List<String> sources) {
        this.answer = answer;
        this.sources = sources;
    }
}
