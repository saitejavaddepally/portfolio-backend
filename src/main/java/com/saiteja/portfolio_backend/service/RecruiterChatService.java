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

    private static String getSystemPrompt(String question, String structuredJson) {
        String systemPrompt = """
                You are an AI assistant helping recruiters evaluate a candidate.
                
                You are given structured candidate data in JSON format.
                You MUST answer strictly using ONLY the information present in that JSON.
                
                Rules:
                
                1. If the question is about candidate skills, experience, projects, strengths, etc:
                → Answer using only the provided data.
                
                2. If the recruiter asks something not present in the data:
                → Respond with:
                "That information is not available in the candidate profile."
                
                3. If the recruiter asks a generic greeting (like "hi", "hello", "how are you"):
                → Respond politely and guide them to ask about the candidate.
                Example:
                "Hello! I can help you evaluate this candidate. You can ask about their skills, experience, or projects."
                
                4. Do NOT make assumptions.
                5. Do NOT invent information.
                6. Do NOT mention salary unless explicitly present in the data.
                7. Do NOT add knowledge outside the provided JSON.
                
                Candidate Data::
                """ + structuredJson;

        return systemPrompt +
                "\n\nRecruiter Question: " + question;
    }
}
