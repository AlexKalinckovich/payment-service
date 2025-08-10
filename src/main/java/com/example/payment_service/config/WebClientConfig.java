package com.example.payment_service.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.random-number-api}")
    private String randomApi;

    @Bean
    @Qualifier("randomApiClient")
    public WebClient randomApiClient() {
        return WebClient.builder()
                .baseUrl(randomApi)
                .build();
    }

}