package com.example.payment_service.service.publishers;

import com.example.payment_service.dto.event.PaymentEventDto;
import com.example.payment_service.model.Payment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventPublisher{
    private final KafkaTemplate<Long, PaymentEventDto> kafka;

    public PaymentEventPublisher(KafkaTemplate<Long, PaymentEventDto> kafka) {
        this.kafka = kafka;
    }

    public void publish(Payment saved) {
        final PaymentEventDto evt = PaymentEventDto.builder()
                .orderId(saved.getOrderId())
                .paymentStatus(saved.getStatus())
                .date(saved.getTimestamp())
                .build();
        kafka.send("create-payment", saved.getOrderId(), evt);
    }
}
