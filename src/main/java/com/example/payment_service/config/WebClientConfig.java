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

    @Value("${services.order-service-api}")
    private String orderServiceApi;

    @Value("${services.user-service-api}")
    private String userServiceApi;

    @Bean
    @Qualifier("randomApiClient")
    public WebClient randomApiClient() {
        return WebClient.builder()
                .baseUrl(randomApi)
                .build();
    }

    @Bean
    @Qualifier("order-service-client")
    public WebClient orderServiceClient() {
        return WebClient.builder()
                .baseUrl(orderServiceApi)
                .build();
    }

    @Bean
    @Qualifier("user-service-client")
    public WebClient userServiceClient() {
        return WebClient.builder()
                .baseUrl(userServiceApi)
                .build();
    }

}