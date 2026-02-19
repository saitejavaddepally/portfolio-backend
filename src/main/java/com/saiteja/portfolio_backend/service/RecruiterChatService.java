package com.saiteja.portfolio_backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saiteja.portfolio_backend.model.AISummary;
import com.saiteja.portfolio_backend.model.ChatMessage;
import com.saiteja.portfolio_backend.repository.AISummaryRepository;
import com.saiteja.portfolio_backend.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruiterChatService {


    private final ChatMessageRepository chatRepository;
    private final AISummaryRepository aiSummaryRepository;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public Flux<String> streamRecruiterAnswer(
            String recruiterEmail,
            String candidateEmail,
            String question
    ) {

        chatRepository.save(ChatMessage.builder()
                .recruiterEmail(recruiterEmail)
                .candidateEmail(candidateEmail)
                .role("user")
                .content(question)
                .createdAt(Instant.now())
                .build());

        AISummary summary = aiSummaryRepository.findByUserEmail(candidateEmail)
                .orElseThrow(() -> new RuntimeException("AI Summary not found"));

        String structuredJson;
        try {
            structuredJson = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(summary.getStructuredSummary());
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize summary");
        }

        List<ChatMessage> lastMessages =
                chatRepository
                        .findTop10ByRecruiterEmailAndCandidateEmailOrderByCreatedAtDesc(
                                recruiterEmail,
                                candidateEmail
                        );

        Collections.reverse(lastMessages);

        List<Message> messages = new ArrayList<>();

        messages.add(new SystemMessage(getSystemPrompt(structuredJson)));

        for (ChatMessage m : lastMessages) {
            if (m.getRole().equals("user")) {
                messages.add(new UserMessage(m.getContent()));
            } else {
                messages.add(new AssistantMessage(m.getContent()));
            }
        }

        messages.add(new UserMessage(question));

        Prompt prompt = new Prompt(messages);

        StringBuilder fullResponse = new StringBuilder();

        return chatModel.stream(prompt)
                .map(chunk -> {
                    String content = chunk.getResult().getOutput().getText();
                    fullResponse.append(content);
                    return content;
                })
                .doOnComplete(() -> {
                    chatRepository.save(ChatMessage.builder()
                            .recruiterEmail(recruiterEmail)
                            .candidateEmail(candidateEmail)
                            .role("assistant")
                            .content(fullResponse.toString())
                            .createdAt(Instant.now())
                            .build());
                });
    }

    private String getSystemPrompt(String structuredJson) {

        return """
                You are an AI assistant helping recruiters quickly evaluate a candidate.
                
                You are provided structured candidate data below.
                
                ========================
                BEHAVIOR RULES
                ========================
                
                1. Greeting Handling:
                   - If the recruiter says "hi", "hello", or small talk,
                     respond warmly and invite them to ask about the candidate.
                
                2. Generic Questions:
                   - If the message is unrelated small talk,
                     politely redirect the recruiter to candidate-related topics.
                
                3. Answer Style:
                   - Be concise, professional, and recruiter-friendly.
                   - Prefer bullet points when listing multiple items.
                   - Keep answers under 6 sentences unless detailed analysis is explicitly requested.
                   - Avoid long dense paragraphs.
                
                4. STRICT FORMATTING RULES (VERY IMPORTANT):
                
                   - Use clean Markdown.
                   - Use "-" for bullet points.
                   - Each bullet MUST be on its own line.
                   - After EVERY bullet, insert a blank line.
                   - Never combine multiple bullets in a single line.
                   - Never use inline star formatting like "*item* text".
                   - Never write bullets without proper line breaks.
                   - Leave one blank line between sections.
                
                   Example format:
                
                   ## Experience Gaps
                
                   - Missing years of experience.
                
                   - Education details not provided.
                
                   - Limited quantified achievements.
                
                5. Content Rules:
                   - Answer ONLY using candidate information.
                   - You may logically infer from experienceHighlights and projectHighlights.
                   - Do NOT hallucinate companies or achievements.
                   - If information is missing but partially inferable, explain what is available.
                   - If truly unrelated, respond exactly with:
                     "That information is not available in the candidate profile."
                
                6. Never mention:
                   - JSON
                   - structured data
                   - provided data
                
                ========================
                Candidate Data:
                """ + structuredJson;
    }
}
