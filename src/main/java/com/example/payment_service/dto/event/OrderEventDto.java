package com.example.payment_service.dto.event;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.NonNull;
import org.hibernate.validator.constraints.Range;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record OrderEventDto(
        @NonNull
        @Range(min = 1)
        Long orderId,

        @NonNull
        @Range(min = 1)
        Long userId,

        @NonNull
        @PastOrPresent
        LocalDateTime date,

        @NonNull
        @Positive
        BigDecimal amount
) { }