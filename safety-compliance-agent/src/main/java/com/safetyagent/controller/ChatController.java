package com.safetyagent.controller;

import com.safetyagent.dto.ChatRequest;
import com.safetyagent.dto.ChatResponse;
import com.safetyagent.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return chatService.getAnswer(request.getQuestion(), request.getCompanyName());
    }
}