package com.saiteja.portfolio_backend.controller;

import com.saiteja.portfolio_backend.service.RecruiterChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class RecruiterChatController {

    private final RecruiterChatService recruiterChatService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamRecruiterChat(
            @RequestParam String candidateEmail,
            @RequestParam String question
    ) {

        return recruiterChatService
                .streamRecruiterAnswer(candidateEmail, question);
    }
}
