package com.saiteja.portfolio_backend.controller;

import com.saiteja.portfolio_backend.model.ChatMessage;
import com.saiteja.portfolio_backend.repository.ChatMessageRepository;
import com.saiteja.portfolio_backend.service.RecruiterChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class RecruiterChatController {

    private final RecruiterChatService recruiterChatService;

    private final ChatMessageRepository chatMessageRepository;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamRecruiterChat(
            @RequestParam String candidateEmail,
            @RequestParam String question,
            Authentication authentication
    ) {
        if (authentication == null ||
                authentication.getAuthorities().stream()
                        .noneMatch(a -> a.getAuthority().equals("ROLE_RECRUITER"))) {
            return Flux.error(new AccessDeniedException("Access Denied"));
        }
        return recruiterChatService
                .streamRecruiterAnswer(authentication.getName(), candidateEmail, question);
    }

    @GetMapping("/history")
    public List<ChatMessage> getChatHistory(
            @RequestParam String recruiterEmail,
            @RequestParam String candidateEmail
    ) {
        return chatMessageRepository
                .findByRecruiterEmailAndCandidateEmailOrderByCreatedAtAsc(
                        recruiterEmail,
                        candidateEmail
                );
    }
}
