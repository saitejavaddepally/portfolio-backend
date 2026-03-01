package com.saiteja.portfolio_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.chat.model.ChatModel;

@Configuration
public class AIModelConfig {

    @Bean("resumeChatModel")
    public ChatModel resumeChatModel(OpenAiChatModel.Builder builder) {

        return builder
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .model("nvidia/llama-nemotron-embed-vl-1b-v2:free")
                                .temperature(0.2)
                                .build()
                )
                .build();
    }

    @Bean("recruiterChatModel")
    public ChatModel recruiterChatModel(OpenAiChatModel.Builder builder) {

        return builder
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .model("nvidia/llama-nemotron-embed-vl-1b-v2:free")
                                .temperature(0.3)
                                .build()
                )
                .build();
    }
}