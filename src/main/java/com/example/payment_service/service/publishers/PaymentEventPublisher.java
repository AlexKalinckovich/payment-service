package com.example.payment_service.service.publishers;

import com.example.payment_service.dto.event.PaymentEventDto;
import com.example.payment_service.model.Payment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentEventPublisher{
    private final KafkaTemplate<Long, String> kafka;
    private final ObjectMapper objectMapper;

    public PaymentEventPublisher(final KafkaTemplate<Long, String> kafka,
                                 final ObjectMapper objectMapper) {
        this.kafka = kafka;
        this.objectMapper = objectMapper;
    }

    public void publish(final Payment saved) {
        final PaymentEventDto evt = PaymentEventDto.builder()
                .paymentId(saved.getId())
                .orderId(saved.getOrderId())
                .paymentStatus(saved.getStatus())
                .date(saved.getTimestamp())
                .build();
        try {
            final String sendEvt = objectMapper.writeValueAsString(evt);
            log.info("Sending {} event to Kafka", evt);
            kafka.send("create-payment", saved.getOrderId(), sendEvt);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}