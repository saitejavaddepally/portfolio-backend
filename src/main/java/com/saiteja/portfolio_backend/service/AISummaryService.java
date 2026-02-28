package com.saiteja.portfolio_backend.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.saiteja.portfolio_backend.model.AISummary;
import com.saiteja.portfolio_backend.repository.AISummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AISummaryService {

    private final ChatModel chatModel;
    private final EmbeddingModel embeddingModel;
    private final AISummaryRepository aiSummaryRepository;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.chat.options.model}")
    private String modelName;

    @Async
    public void generateAndSaveSummary(String userEmail,String userId, Map<String, Object> portfolioData) {

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
                              "yearsOfExperience": 0,
                              "coreSkills": [],
                              "workExperience": [
                                {
                                  "company": "",
                                  "role": "",
                                  "duration": "",
                                  "keyResponsibilities": [],
                                  "achievements": [],
                                  "technologiesUsed": []
                                }
                              ],
                            
                              "projects": [
                                {
                                  "name": "",
                                  "description": "",
                                  "technologiesUsed": [],
                                  "impact": ""
                                }
                              ],
                            
                              "education": {
                                "institution": "",
                                "degree": "",
                                "fieldOfStudy": "",
                                "highlights": ""
                              }
                            }
                            Portfolio Data:
                            %s
                            """.formatted(portfolioJson);

            String aiResponse = chatModel.call(systemPrompt + "\n" + userPrompt);

            Map<String, Object> structuredJson =
                    objectMapper.readValue(aiResponse, new TypeReference<Map<String, Object>>() {});

            String embeddingText = buildEmbeddingText(structuredJson);

            List<Double> embedding = generateEmbedding(embeddingText);

            AISummary summary = aiSummaryRepository
                    .findByUserEmail(userEmail)
                    .orElse(
                            AISummary.builder()
                                    .userEmail(userEmail)
                                    .userId(userId)
                                    .model(modelName)
                                    .createdAt(Instant.now())
                                    .build()
                    );

            summary.setStructuredSummary(structuredJson);
            summary.setUpdatedAt(Instant.now());
            summary.setEmbeddingText(embeddingText);
            summary.setEmbedding(embedding);

            aiSummaryRepository.save(summary);

            System.out.println("Ai Summary successfully stored " + summary);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate AI summary", e);
        }
    }


    private String buildEmbeddingText(Map<String, Object> summary) {

        StringBuilder sb = new StringBuilder();

        sb.append("Candidate Overview:\n\n");

        if (summary.get("professionalSummary") != null) {
            sb.append(summary.get("professionalSummary")).append("\n\n");
        }

        if (summary.get("yearsOfExperience") != null) {
            sb.append("Years of Experience: ")
                    .append(summary.get("yearsOfExperience"))
                    .append("\n\n");
        }

        if (summary.get("coreSkills") instanceof List<?> skills) {
            sb.append("Core Skills: ");
            sb.append(String.join(", ",
                    skills.stream().map(Object::toString).toList()));
            sb.append("\n\n");
        }

        return sb.toString();
    }

    private List<Double> generateEmbedding(String text) {

        EmbeddingResponse response =
                embeddingModel.embedForResponse(List.of(text));

        List<Double> embeddings = new ArrayList<>();

        for(float embedding : response.getResults().getFirst().getOutput()){
            embeddings.add((double) embedding);
        }

        return embeddings;
    }
}