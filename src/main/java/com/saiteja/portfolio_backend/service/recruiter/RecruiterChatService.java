package com.saiteja.portfolio_backend.service.recruiter;

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

        // Save user message
        chatRepository.save(ChatMessage.builder()
                .recruiterEmail(recruiterEmail)
                .candidateEmail(candidateEmail)
                .role("user")
                .content(question)
                .createdAt(Instant.now())
                .build());

        AISummary summary = aiSummaryRepository
                .findByUserEmail(candidateEmail)
                .orElseThrow(() -> new RuntimeException("AI Summary not found"));

        String structuredJson;
        try {
            structuredJson = objectMapper
                    .writeValueAsString(summary.getStructuredSummary());
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize summary");
        }

        List<ChatMessage> lastMessages =
                chatRepository
                        .findTop4ByRecruiterEmailAndCandidateEmailOrderByCreatedAtDesc(
                                recruiterEmail,
                                candidateEmail
                        );

        Collections.reverse(lastMessages);

        List<Message> messages = new ArrayList<>();

        messages.add(new SystemMessage(getSystemPrompt()));
        messages.add(new SystemMessage("Candidate Data:\n" + structuredJson));

        for (ChatMessage m : lastMessages) {
            if ("user".equals(m.getRole())) {
                messages.add(new UserMessage(m.getContent()));
            } else {
                messages.add(new AssistantMessage(m.getContent()));
            }
        }

        messages.add(new UserMessage(question));

        Prompt prompt = new Prompt(messages);

        StringBuilder fullResponse = new StringBuilder();

        return chatModel.stream(prompt)
                .map(chunk -> chunk.getResult().getOutput().getText())
                .filter(text -> text != null && !text.isBlank())
                .doOnNext(fullResponse::append)
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

    private String getSystemPrompt() {

        return """
            You are an AI assistant helping recruiters evaluate a candidate.
            
            You are given structured candidate data below. Use ONLY this information.
            
            BEHAVIOR:
            
            - If greeted (hi/hello/small talk), respond warmly and invite a candidate-related question.
            - If unrelated small talk, politely redirect to candidate evaluation.
            
            ANSWER STYLE:
            
            - Be concise and professional.
            - Prefer bullet points when listing items.
            - Maximum 6 sentences unless detailed analysis is explicitly requested.
            - Avoid dense paragraphs.
            
            FORMATTING RULES (STRICT):
            
            - Use clean Markdown.
            - Use "-" for bullets.
            - Each bullet must be on its own line.
            - Insert one blank line after every bullet.
            - Leave one blank line between sections.
            - Do not use inline star formatting.
            - Do not combine bullets in one line.
            
            CONTENT RULES:
            
            - Answer strictly using candidate data.
            - You may logically infer from experienceHighlights and projectHighlights.
            - Do NOT hallucinate companies or achievements.
            - If information is missing but partially inferable, explain what is available.
            - If completely unavailable, respond exactly with:
              "That information is not available in the candidate profile."
            
            Never mention JSON, structured data, or that data was provided.
            
            Candidate Data:
            """ ;
    }
}
