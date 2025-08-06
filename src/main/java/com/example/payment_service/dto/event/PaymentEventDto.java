package com.example.payment_service.dto.event;

import com.example.payment_service.model.PaymentStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PaymentEventDto(
        String paymentId,
        Long orderId,
        LocalDateTime date,
        PaymentStatus paymentStatus
) { }
