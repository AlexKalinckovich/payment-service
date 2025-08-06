package com.example.payment_service.dto.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record OrderEventDto(
        Long orderId,
        Long userId,
        LocalDateTime date,
        BigDecimal amount
) { }