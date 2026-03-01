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

        Convert the resume into STRICT valid JSON
        matching EXACTLY the required UI schema.

        GLOBAL RULES:
        - Return ONLY valid JSON.
        - No explanations.
        - No markdown.
        - No backticks.
        - No comments.
        - No hallucinated companies.
        - If data is missing, use empty arrays or empty strings.
        - All description fields must be arrays.

        HERO SECTION RULES (CRITICAL):

        The hero section is NOT raw resume extraction.
        It must be professionally composed and branding-focused.

        hero.intro must follow this style:

        {
          "desc": [""],     // Keep minimal. Can contain one short sentence or be empty.
          "highlight": "",  // Main tech stack or strongest skill keyword (e.g., "Java & Spring Boot")
          "text": "",       // Short prefix phrase like: "I build scalable systems with"
          "suffix": ""      // 1–3 line professional branding summary written naturally
        }

        INSTRUCTIONS FOR HERO GENERATION:

        - Do NOT copy resume bullet points.
        - Do NOT list achievements here.
        - This is a professional branding summary.
        - Keep it clean and impactful.
        - highlight → strongest technology stack.
        - text → short starting phrase.
        - suffix → professional 2–3 line identity statement.
        - Avoid generic phrases like "hardworking individual".
        - Make it sound modern and confident.

        Example Style:

        "intro": {
            "desc": [""],
            "highlight": "Java, Spring Boot & Distributed Systems",
            "text": "I build scalable enterprise systems with",
            "suffix": "a strong focus on security, performance, and automation. I enjoy solving complex backend challenges and transforming business problems into reliable technical solutions."
        }

        EXPERIENCE RULES:
        - experience must be array.
        - description must be array of bullet points.
        - Preserve metrics like %, TPS, reductions.

        SKILLS:
        - Only technical skills.
        - No soft skills.

        EDUCATION:
        - Convert properly.

        SLUG:
        - Lowercase full name with hyphens.

        Return ONLY valid JSON.
        """;
    }
}