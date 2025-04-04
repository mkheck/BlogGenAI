package com.thehecklers.BlogGenAI;

import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.KeyCredential;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.ollama.OllamaChatModel;
//import org.springframework.ai.ollama.api.OllamaApi;
//import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BlogGenConfig {
    @Value("${spring.ai.openai.api-key:missing_api_key}")
    String oaiApiKey;
    @Value("${spring.ai.azure.openai.api-key:missing_api_key}")
    String aoaiApiKey;
    @Value("${spring.ai.openai.endpoint:missing_endpoint}")
    String oaiEndpoint;
    @Value("${spring.ai.azure.openai.endpoint:missing_endpoint}")
    String aoaiEndpoint;
//    @Value("${spring.ai.ollama.chat.options.model:missing_model}")
//    String ollamaModel;

//    @Bean(name = "ollamaChatModel")
//    public OllamaChatModel getOllamaChatModel() {
//        return OllamaChatModel.builder()
//                .ollamaApi(new OllamaApi())
//                .defaultOptions(OllamaOptions.builder()
//                        .model(ollamaModel)
//                        .build())
//                .build();
//    }

//    @Bean(name = "ollamaClient")
//    public ChatClient ollamaClient() {
//        return ChatClient.builder(getOllamaChatModel())
//                .build();
//    }

//    @Bean
//    public OpenAIClient openAiClient() {
////        return OpenAiClient.builder()
////                .openAiApi(OpenAiApi.builder()
////                        .apiKey(apiKey)
////                        .baseUrl(endpoint)
////                        .build())
////                .build();
//        return new OpenAIClientBuilder().buildClient();
//    }

    @Bean(name = "openaiChatClient")
    public ChatClient openaiChatClient() {
        return ChatClient.builder(OpenAiChatModel.builder()
                        .openAiApi(OpenAiApi.builder()
                                .apiKey(oaiApiKey)
//                                .baseUrl(endpoint)  MH: For OpenAI proper, endpoint is default
                                .build())
                        .build())
                .build();
    }

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
