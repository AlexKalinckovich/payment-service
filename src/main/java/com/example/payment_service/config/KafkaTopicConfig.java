package com.example.payment_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic createOrderTopic() {
        return TopicBuilder
                .name("create-order-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic createPaymentTopic() {
        return TopicBuilder
                .name("create-payment-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

}
