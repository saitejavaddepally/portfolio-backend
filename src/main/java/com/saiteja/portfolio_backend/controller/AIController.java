package com.saiteja.portfolio_backend.controller;

import com.saiteja.portfolio_backend.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @PostMapping("/parse-resume")
    public ResponseEntity<Map<String, Object>> parseResume(
            @RequestBody String resumeText
    ) {
        return ResponseEntity.ok(
                aiService.parseResume(resumeText)
        );
    }
}