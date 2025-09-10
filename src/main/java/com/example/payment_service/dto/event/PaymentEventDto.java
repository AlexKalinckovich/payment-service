package com.example.payment_service.dto.event;

import com.example.payment_service.model.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDateTime;

@Builder
public record PaymentEventDto(
        @NotBlank
        String paymentId,

        @NotNull
        @Range(min = 1)
        Long orderId,

        @NotNull
        @PastOrPresent
        LocalDateTime date,

        @NotNull
        PaymentStatus paymentStatus
) { }
