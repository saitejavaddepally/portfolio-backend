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

    private final ChatModel recruiterChatModel;
    private final ObjectMapper objectMapper;

    public Map<String, Object> parseResume(String resumeText) {

        long startTime = System.currentTimeMillis();
        logger.info("AI resume parsing started");

        try {

            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(getSystemPrompt()),
                    new UserMessage(resumeText)
            ));

            String response = recruiterChatModel.call(prompt)
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
            You are a resume-to-portfolio transformer.
        
            Convert resume text into STRICT valid JSON that EXACTLY matches the frontend schema.
        
            RULES:
            - Return ONLY valid JSON.
            - No explanations.
            - No markdown.
            - No extra properties.
            - Do NOT rename fields.
            - If data missing, return empty arrays [] or empty strings "".
            - Never return null or undefined.
            - Never omit required arrays.
        
            REQUIRED STRUCTURE:
        
            {
              "hero": {
                "name": "",
                "roles": [],
                "intro": {
                  "desc": [""],
                  "highlight": "",
                  "suffix": "",
                  "text": ""
                },
                "image": ""
              },
              "socials": [],
              "experience": [
              {
                  "company": "",
                  "role": "",
                  "logo": "",
                  "dates": "",
                  "url": "",
                  "description": []
              ],
              "projects": [],
              "achievements": {
                "type": "",
                "title": "",
                "org": "",
                "image": "",
                "description": "",
                "items": []
              },
                "education": [
                    {
                        "institution": "",
                        "location": "",
                        "degree": "",
                        "dates": "",
                        "desc": []
                    }
                ],
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
        
            MAPPING RULES:
            - Don't update slug and isPublished on any condition
            - Contact information should be included in socials
            - position → role
            - startDate + endDate → "dates": "MM/YYYY - MM/YYYY" or "MM/YYYY - Present"
            - onlineProfiles → codingProfiles
            - hero.contacts must be array of { type, value, label }
            - experience.description, education.desc, projects.desc, achievements.items must be arrays
        
            HERO INTRO RULES:
        
            - NOT resume extraction.
            - Professionally composed branding summary.
            - desc: minimal or [""]
            - highlight: strongest tech stack (short, not sentence)
            - text: short prefix phrase (no period)
            - suffix: 1–3 professional sentences
            - Do NOT copy bullets.
            - Do NOT list metrics.
        
            Return ONLY JSON.
        """;
    }
}