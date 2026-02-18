package com.saiteja.portfolio_backend.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.saiteja.portfolio_backend.model.AISummary;
import com.saiteja.portfolio_backend.repository.AISummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AISummaryService {

    private final ChatModel chatModel;
    private final AISummaryRepository aiSummaryRepository;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.chat.options.model}")
    private String modelName;

    @Async
    public void generateAndSaveSummary(String userEmail, Map<String, Object> portfolioData) {

        try {

            String portfolioJson = objectMapper.writeValueAsString(portfolioData);

            String systemPrompt = """
                                You are an AI that generates structured professional summaries.
                                
                                STRICT RULES:
                                - Use ONLY provided data.
                                - DO NOT hallucinate.
                                - If information is missing, return null.
                                - Return STRICT JSON only.
                                - Do NOT add explanations.
                                """;

            String userPrompt = """
                            Generate a structured JSON summary with EXACTLY this schema:
                            
                            {
                              "professionalSummary": "",
                              "coreSkills": [],
                              "experienceHighlights": [],
                              "projectHighlights": [],
                              "educationSummary": "",
                              "yearsOfExperience": 0
                            }

                            Portfolio Data:
                            %s
                            """.formatted(portfolioJson);

            String aiResponse = chatModel.call(systemPrompt + "\n" + userPrompt);

            Map<String, Object> structuredJson =
                    objectMapper.readValue(aiResponse, new TypeReference<Map<String, Object>>() {});

            AISummary summary = aiSummaryRepository
                    .findByUserEmail(userEmail)
                    .orElse(
                            AISummary.builder()
                                    .userEmail(userEmail)
                                    .model(modelName)
                                    .createdAt(Instant.now())
                                    .build()
                    );

            summary.setStructuredSummary(structuredJson);
            summary.setUpdatedAt(Instant.now());

            aiSummaryRepository.save(summary);

            System.out.println("Ai Summary successfully stored " + summary);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate AI summary", e);
        }
    }
}