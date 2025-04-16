package com.thehecklers.BlogGenAI;

import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.KeyCredential;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Uncomment this annotation to enable this configuration
//@Configuration
public class BlogGenConfig {
    @Value("${spring.ai.azure.openai.api-key:missing_api_key}")
    String aoaiApiKey;
    @Value("${spring.ai.openai.endpoint:missing_endpoint}")
    String aoaiEndpoint;

    @Bean(name = "azureOpenAiChatClient")
    public ChatClient azureopenaiChatClient() {
        return ChatClient.builder(azureOpenAiChatModel())
                .build();
    }

    @Bean(name = "azureOpenAiChatModel")
    public AzureOpenAiChatModel azureOpenAiChatModel() {
        return AzureOpenAiChatModel.builder()
                .openAIClientBuilder(new OpenAIClientBuilder()
                        .credential(new KeyCredential(aoaiApiKey))
                        .endpoint(aoaiEndpoint))
                .build();
    }
}
