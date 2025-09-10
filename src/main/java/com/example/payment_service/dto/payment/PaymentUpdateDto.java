package com.example.payment_service.dto.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentUpdateDto {
    @NotNull
    @Range(min = 1)
    private String id;

    @NotNull
    @Range(min = 1)
    private Long orderId;

    @NotNull
    @Range(min = 1)
    private Long userId;

    @Positive
    private BigDecimal paymentAmount;
}
