package com.saiteja.portfolio_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saiteja.portfolio_backend.model.AISummary;
import com.saiteja.portfolio_backend.repository.AISummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class RecruiterChatService {

    private final AISummaryRepository aiSummaryRepository;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public Flux<String> streamRecruiterAnswer(String candidateEmail, String question) {
        AISummary summary = aiSummaryRepository.findByUserEmail(candidateEmail)
                .orElseThrow(() -> new RuntimeException("AI Summary not found"));

        try {

            String structuredJson =
                    objectMapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(summary.getStructuredSummary());

            String fullPrompt = getSystemPrompt(question, structuredJson);

            return chatModel.stream(fullPrompt)
                    .filter(token -> token != null && !token.isBlank());

        } catch (Exception e) {
            return Flux.error(new RuntimeException("Streaming failed", e));
        }
    }

    private String getSystemPrompt(String question, String structuredJson) {

        return """
            You are an AI assistant helping recruiters evaluate a candidate.
            
            You are provided with structured candidate data in JSON format below.
            
            RULES:
            1. Answer ONLY using the information in the JSON.
            2. If the question refers to experience, projects, or skills, infer from related fields (like experienceHighlights).
            3. If the exact company name is not mentioned but relevant experience exists, explain based on related highlights.
            4. If truly unrelated to the candidate profile, politely say:
               "That information is not available in the candidate profile."
            5. Always answer in a professional, recruiter-friendly tone.
            6. Never mention "JSON" or say "based on provided data".
            
            Candidate Data:
            """ + structuredJson + """
            
            Recruiter Question:
            """ + question;
    }
}
