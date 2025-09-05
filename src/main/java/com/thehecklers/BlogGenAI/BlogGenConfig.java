package com.thehecklers.BlogGenAI;

import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.KeyCredential;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.concurrent.TimeUnit;

// Uncomment this annotation to enable this configuration
//@Configuration
public class BlogGenConfig {
    @Value("${spring.ai.azure.openai.api-key:missing_api_key}")
    String aoaiApiKey;
    @Value("${spring.ai.openai.endpoint:missing_endpoint}")
    String aoaiEndpoint;

	@Bean(name = "azureOpenAiChatClient")
	ChatClient azureopenaiChatClient() {
        return ChatClient.builder(azureOpenAiChatModel())
                .build();
    }

	@Bean(name = "azureOpenAiChatModel")
	AzureOpenAiChatModel azureOpenAiChatModel() {
        return AzureOpenAiChatModel.builder()
                .openAIClientBuilder(new OpenAIClientBuilder()
                        .credential(new KeyCredential(aoaiApiKey))
                        .endpoint(aoaiEndpoint))
                .build();
    }

	@Bean
	WebClient webClient() {
        TcpClient tcpClient = TcpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000) // Connection timeout
                .doOnConnected(connection ->
                        connection.addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))); // Read timeout

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .build();
    }
}
