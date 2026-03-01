package com.saiteja.portfolio_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIService.class);

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public Map<String, Object> parseResume(String resumeText) {

        long startTime = System.currentTimeMillis();
        logger.info("AI resume parsing started");

        try {

            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(getSystemPrompt()),
                    new UserMessage(resumeText)
            ));

            String response = chatModel.call(prompt)
                    .getResult()
                    .getOutput()
                    .getText();

            if (response == null || response.isBlank()) {
                throw new RuntimeException("Empty AI response");
            }

            // Convert AI JSON → Map
            Map<String, Object> result =
                    objectMapper.readValue(response, Map.class);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("AI resume parsing completed in {}ms", duration);

            return result;

        } catch (Exception e) {
            logger.error("AI resume parsing failed: {}", e.getMessage(), e);
            throw new RuntimeException("AI resume parsing failed");
        }
    }

    private String getSystemPrompt() {

        return """
        You are a professional resume-to-portfolio transformer.

        Convert the resume text into STRICT valid JSON
        matching EXACTLY the required UI schema.

        ABSOLUTE RULES:
        - Return ONLY valid JSON.
        - No explanations.
        - No markdown.
        - No backticks.
        - No comments.
        - No trailing commas.
        - No hallucinated companies.
        - If data is missing, use empty arrays or empty strings.
        - NEVER return paragraph descriptions.
        - EVERY description field MUST be an array of bullet strings.
        - Each bullet must be its own string element.
        - Never combine multiple points into one string.
        - Never return a single long paragraph.

        REQUIRED OUTPUT STRUCTURE:

        {
          "hero": {
            "name": "",
            "roles": [],
            "intro": {
              "desc": [],
              "highlight": "",
              "suffix": "",
              "text": ""
            },
            "image": ""
          },
          "socials": [],
          "experience": [],
          "projects": [],
          "achievements": {},
          "education": [],
          "skills": [],
          "footer": {
            "title": "",
            "subtitle": "",
            "email": "",
            "socials": []
          },
          "activeTemplate": "medium",
          "slug": "",
          "isPublished": false,
          "codingProfiles": []
        }

        BULLET ENFORCEMENT (CRITICAL):

        - hero.intro.desc → MUST be an array of short bullet points.
        - experience[].description → MUST be an array of bullet points.
        - projects[].desc → MUST be an array of bullet points.
        - education[].desc → MUST be an array of bullet points.
        - achievements.items → MUST be an array of bullet points.

        - Every responsibility must be a separate string.
        - Preserve metrics like percentages, TPS, time reductions.
        - Keep technical terms intact.
        - Do NOT compress bullets.
        - Do NOT merge bullets.

        EXPERIENCE FORMAT EXAMPLE:

        {
          "company": "DBS Bank",
          "role": "Full Stack Developer",
          "logo": "https://www.google.com/s2/favicons?domain=dbs.com&sz=128",
          "dates": "07/2022 - Present",
          "url": "https://www.dbs.com/",
          "description": [
            "Designed secure Consent Management System enabling partner banks to access account details.",
            "Integrated Spring AOP for fine-grained data authorization.",
            "Automated Apache upgrade reducing effort from 5 hours to 30 minutes."
          ]
        }

        FINAL INSTRUCTION:
        Return ONLY valid JSON.
        """;
    }
}