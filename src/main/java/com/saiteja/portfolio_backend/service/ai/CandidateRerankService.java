package com.saiteja.portfolio_backend.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CandidateRerankService {

    private final ChatClient chatClient;

    public String rerankCandidates(String jobQuery, List<String> candidateSummaries) {

        String prompt = """
        You are helping rank candidates for a job.

        Job Requirement:
        %s

        Candidates:
        %s

        Rank the candidates based on best match to the job.
        Return only the ranked candidate numbers.
        """.formatted(jobQuery, candidateSummaries);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}