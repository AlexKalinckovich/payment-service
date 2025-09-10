package com.example.payment_service.service.payment;

import com.example.payment_service.dto.payment.PaymentCreateDto;
import com.example.payment_service.dto.payment.PaymentResponseDto;
import com.example.payment_service.dto.payment.PaymentUpdateDto;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.naming.ServiceUnavailableException;
import java.util.List;

public interface PaymentService {

    PaymentResponseDto createPaymentNoApi(PaymentCreateDto paymentCreateDto) throws ServiceUnavailableException, JsonProcessingException;

    void createPayment(PaymentCreateDto paymentCreateDto) throws ServiceUnavailableException, JsonProcessingException;

    PaymentResponseDto getPaymentById(String id);

    List<PaymentResponseDto> getPaymentByOrderId(Long orderId);

    List<PaymentResponseDto> getPaymentByUserId(Long userId);

    List<PaymentResponseDto> getPaymentsByIds(List<String> ids);

    PaymentResponseDto updatePayment(PaymentUpdateDto paymentUpdateDto);

    PaymentResponseDto deletePayment(String id);

}
