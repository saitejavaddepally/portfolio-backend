package com.saiteja.portfolio_backend.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueryRewriteService {

    private final ChatClient chatClient;

    public String rewriteQuery(String recruiterQuery) {

        String prompt = """
        You are an assistant that converts recruiter queries into clean search queries.

        Extract only the important hiring signals:
        - Role
        - Skills
        - Technologies
        - Experience if mentioned

        Remove unnecessary words.

        Return a short search query.

        Recruiter Query:
        %s
        """.formatted(recruiterQuery);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}