package com.example.payment_service.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCreateDto {
    private Long orderId;
    private Long userId;
    private LocalDateTime timestamp;
    private BigDecimal paymentAmount;
}
