package com.example.payment_service.controller;

import com.example.payment_service.dto.payment.PaymentCreateDto;
import com.example.payment_service.dto.payment.PaymentResponseDto;
import com.example.payment_service.service.payment.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.ServiceUnavailableException;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/create/noApi")
    public ResponseEntity<PaymentResponseDto> createPaymentNoApi(@RequestBody  final PaymentCreateDto paymentCreateDto) throws ServiceUnavailableException, JsonProcessingException {
        final PaymentResponseDto paymentResponseDto = paymentService.createPaymentNoApi(paymentCreateDto);
        return ResponseEntity.ok(paymentResponseDto);
    }
}
