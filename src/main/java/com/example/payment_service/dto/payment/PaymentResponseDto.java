package com.example.payment_service.dto.payment;

import com.example.payment_service.model.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record PaymentResponseDto(
        String id,
        String orderId,
        String userId,
        PaymentStatus status,
        LocalDateTime timeStamp,
        BigDecimal paymentAmount
) {}
