package com.saiteja.portfolio_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.chat.model.ChatModel;

@Configuration
public class AIModelConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Bean
    public OpenAiApi openAiApi() {
        // Use the builder pattern instead of the constructor
        return OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
    }

    @Bean("resumeChatModel")
    public ChatModel resumeChatModel(OpenAiApi openAiApi) {
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .model("nvidia/nemotron-nano-9b-v2:free")
                                .temperature(0.2)
                                .build()
                )
                .build();
    }

    @Bean("recruiterChatModel")
    public ChatModel recruiterChatModel(OpenAiApi openAiApi) {
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .model("stepfun/step-3.5-flash:free")
                                .temperature(0.3)
                                .build()
                )
                .build();
    }
}