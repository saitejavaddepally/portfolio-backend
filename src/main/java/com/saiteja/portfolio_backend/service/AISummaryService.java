package com.saiteja.portfolio_backend.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.saiteja.portfolio_backend.model.AISummary;
import com.saiteja.portfolio_backend.repository.AISummaryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(AISummaryService.class);

    private final ChatModel chatModel;
    private final EmbeddingModel embeddingModel;
    private final AISummaryRepository aiSummaryRepository;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.chat.options.model}")
    private String modelName;

    @Async
    public void generateAndSaveSummary(String userEmail, String userId, Map<String, Object> portfolioData) {

        long startTime = System.currentTimeMillis();
        logger.info("AI Summary generation started for email: {} - userId: {}", userEmail, userId);

        try {

            logger.debug("Serializing portfolio data for email: {}", userEmail);
            String portfolioJson = objectMapper.writeValueAsString(portfolioData);
            logger.trace("Portfolio JSON: {}", portfolioJson);

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

            logger.debug("Calling ChatModel for AI summary generation (model: {}) for email: {}",
                modelName, userEmail);

            String aiResponse = chatModel.call(systemPrompt + "\n" + userPrompt);
            logger.trace("AI Response received for email: {}", userEmail);

            logger.debug("Parsing AI response JSON for email: {}", userEmail);
            Map<String, Object> structuredJson =
                    objectMapper.readValue(aiResponse, new TypeReference<Map<String, Object>>() {});

            logger.trace("Structured JSON parsed successfully for email: {}", userEmail);

            String embeddingText = buildEmbeddingText(structuredJson);
            logger.debug("Embedding text built (length: {}) for email: {}",
                embeddingText.length(), userEmail);

            logger.debug("Generating embedding vector for email: {}", userEmail);
            List<Double> embedding = generateEmbedding(embeddingText);
            logger.debug("Embedding generated successfully (dimensions: {}) for email: {}",
                embedding.size(), userEmail);

            AISummary summary = aiSummaryRepository
                    .findByUserEmail(userEmail)
                    .orElse(
                            AISummary.builder()
                                    .userEmail(userEmail)
                                    .model(modelName)
                                    .createdAt(Instant.now())
                                    .build()
                    );

            summary.setUserId(userId);
            summary.setStructuredSummary(structuredJson);
            summary.setUpdatedAt(Instant.now());
            summary.setEmbeddingText(embeddingText);
            summary.setEmbedding(embedding);

            aiSummaryRepository.save(summary);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("AI Summary successfully generated and saved for email: {} - Duration: {}ms",
                userEmail, duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Failed to generate AI summary for email: {} - Duration: {}ms - Error: {}",
                userEmail, duration, e.getMessage(), e);
            throw new RuntimeException("Failed to generate AI summary", e);
        }
    }


    private String buildEmbeddingText(Map<String, Object> summary) {

        logger.debug("Building embedding text from AI summary");

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

        logger.trace("Embedding text built with length: {}", sb.length());
        return sb.toString();
    }

    private List<Double> generateEmbedding(String text) {

        logger.debug("Generating embedding for text (length: {})", text.length());

        EmbeddingResponse response =
                embeddingModel.embedForResponse(List.of(text));

        List<Double> embeddings = new ArrayList<>();

        for(float embedding : response.getResults().getFirst().getOutput()){
            embeddings.add((double) embedding);
        }

        logger.debug("Embedding generation completed - Dimensions: {}", embeddings.size());
        return embeddings;
    }
}