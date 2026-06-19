package com.mailmind.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.Executor;

@Configuration
public class AppConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
            .codecs(configurer -> configurer
                .defaultCodecs()
                .maxInMemorySize(10 * 1024 * 1024) // 10MB buffer for large responses
            );
    }

    @Bean
    public WebClient geminiWebClient(WebClient.Builder builder) {
        return builder
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();
    }

    @Bean
    public WebClient nvidiaWebClient(WebClient.Builder builder) {
        return builder
            .baseUrl("https://integrate.api.nvidia.com/v1")
            .build();
    }

    @Bean(name = "gmailSyncExecutor")
    public Executor gmailSyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("gmail-sync-");
        executor.initialize();
        return executor;
    }
}
