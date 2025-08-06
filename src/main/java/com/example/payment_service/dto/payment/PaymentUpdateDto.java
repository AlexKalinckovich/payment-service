package com.example.payment_service.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentUpdateDto {
    private String id;
    private String orderId;
    private String userId;
    private LocalDateTime timestamp;
    private BigDecimal paymentAmount;
}
