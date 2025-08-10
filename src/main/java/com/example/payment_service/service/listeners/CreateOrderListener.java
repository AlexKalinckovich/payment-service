package com.example.payment_service.service.listeners;

import com.example.payment_service.dto.event.OrderEventDto;
import com.example.payment_service.dto.payment.PaymentCreateDto;
import com.example.payment_service.service.payment.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.naming.ServiceUnavailableException;

@Service
@Slf4j
public class CreateOrderListener {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public CreateOrderListener(final PaymentService paymentService,
                               final ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "create-order", groupId = "payment-group")
    public void onNewOrder(final String orderEvent) throws ServiceUnavailableException {
        log.info("Received order event {}", orderEvent);
        final OrderEventDto orderEventDto;
        try {
            orderEventDto = objectMapper.readValue(orderEvent, OrderEventDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        final PaymentCreateDto dto = PaymentCreateDto.builder()
                .orderId(orderEventDto.orderId())
                .userId(orderEventDto.userId())
                .timestamp(orderEventDto.date())
                .paymentAmount(orderEventDto.amount())
                .build();
        paymentService.createPayment(dto);
    }
}
