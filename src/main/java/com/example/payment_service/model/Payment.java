package com.example.payment_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Payment {
    @Id
    private String id;
    private Long orderId;
    private Long userId;
    private PaymentStatus status;
    private LocalDateTime timestamp;
    private BigDecimal paymentAmount;
}
