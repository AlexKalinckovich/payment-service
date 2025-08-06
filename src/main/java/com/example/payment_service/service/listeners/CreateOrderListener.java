package com.example.payment_service.service.listeners;

import com.example.payment_service.dto.event.OrderEventDto;
import com.example.payment_service.dto.payment.PaymentCreateDto;
import com.example.payment_service.service.payment.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.naming.ServiceUnavailableException;

@Service
public class CreateOrderListener {

    private final PaymentService paymentService;

    public CreateOrderListener(final PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "create-order", groupId = "payment-group")
    public void onNewOrder(final OrderEventDto orderEvent) throws ServiceUnavailableException {
        final PaymentCreateDto dto = PaymentCreateDto.builder()
                .orderId(orderEvent.orderId())
                .userId(orderEvent.userId())
                .timestamp(orderEvent.date())
                .paymentAmount(orderEvent.amount())
                .build();
        paymentService.createPayment(dto);
    }
}
