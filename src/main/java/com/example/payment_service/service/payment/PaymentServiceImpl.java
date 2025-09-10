package com.example.payment_service.service.payment;

import com.example.payment_service.dto.payment.PaymentCreateDto;
import com.example.payment_service.dto.payment.PaymentResponseDto;
import com.example.payment_service.dto.payment.PaymentUpdateDto;
import com.example.payment_service.exception.exception.PaymentNotFoundException;
import com.example.payment_service.mapper.PaymentMapper;
import com.example.payment_service.model.Payment;
import com.example.payment_service.model.PaymentStatus;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.service.orderServiceApi.OrderServiceApi;
import com.example.payment_service.service.publishers.PaymentEventPublisher;
import com.example.payment_service.service.randomNumberApi.RandomNumberService;
import com.example.payment_service.validator.PaymentValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.naming.ServiceUnavailableException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@Qualifier("paymentServiceImpl")
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final int MIN_RANDOM_VALUE = 1;
    private static final int MAX_RANDOM_VALUE = 2000;

    private final RandomNumberService randomNumberService;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentValidator paymentValidator;
    private final PaymentEventPublisher paymentEventPublisher;
    private final OrderServiceApi orderServiceApi;

    @Override
    public PaymentResponseDto createPaymentNoApi(final PaymentCreateDto paymentCreateDto) throws ServiceUnavailableException, JsonProcessingException {
        final Payment entity = preCreateInit(paymentCreateDto);

        final BigDecimal orderTotal = orderServiceApi.getOrderTotal(paymentCreateDto.getOrderId())
                .blockOptional(Duration.ofSeconds(3))
                .orElseThrow(() -> new ServiceUnavailableException("Order service timeout"));

        if (paymentCreateDto.getPaymentAmount().compareTo(orderTotal) >= 0) {
            entity.setStatus(PaymentStatus.SUCCESS);
        } else {
            entity.setStatus(PaymentStatus.FAILED);
        }

        final Payment saved = paymentRepository.save(entity);
        paymentEventPublisher.publish(saved);
        return paymentMapper.toResponseDto(saved);
    }


    @Override
    public void createPayment(final PaymentCreateDto paymentCreateDto) throws ServiceUnavailableException, JsonProcessingException {
        final Payment entity = preCreateInit(paymentCreateDto);
        final List<Long> numbers = randomNumberService.getRandomNumber(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE, 1)
                .blockOptional(Duration.ofSeconds(3))
                .orElseThrow(() -> new ServiceUnavailableException("Random service timeout"));

        if (!numbers.isEmpty()) {
            final long n = numbers.getFirst();
            entity.setStatus(n % 2 == 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
            final Payment saved = paymentRepository.save(entity);
            paymentEventPublisher.publish(saved);
        } else {
            throw new ServiceUnavailableException("No numbers returned");
        }
    }

    @Override
    public PaymentResponseDto getPaymentById(final String id) {
        final Optional<Payment> payment = paymentRepository.findById(id);
        if(payment.isEmpty()){
            throw new PaymentNotFoundException(id);
        }

        return paymentMapper.toResponseDto(payment.get());
    }

    @Override
    public List<PaymentResponseDto> getPaymentByOrderId(final Long orderId) {
        final List<Payment> payments = paymentRepository.getPaymentByOrderId(orderId);
        return paymentMapper.toResponseDtoList(payments);
    }

    @Override
    public List<PaymentResponseDto> getPaymentByUserId(final Long userId) {
        final List<Payment> payments = paymentRepository.getPaymentByUserId(userId);
        return paymentMapper.toResponseDtoList(payments);
    }


    @Override
    public List<PaymentResponseDto> getPaymentsByIds(final List<String> ids) {
        final List<Payment> payments = paymentValidator.validatedPaymentsExistenceByIds(ids);
        return paymentMapper.toResponseDtoList(payments);
    }

    @Override
    public PaymentResponseDto updatePayment(final PaymentUpdateDto paymentUpdateDto) {
        final Payment payment = paymentValidator.validateUpdateDto(paymentUpdateDto);
        paymentMapper.updateEntity(paymentUpdateDto, payment);
        final Payment updated = paymentRepository.save(payment);
        return paymentMapper.toResponseDto(updated);
    }

    @Override
    public PaymentResponseDto deletePayment(final String id) {
        final Payment payment = paymentValidator.validatePaymentExistence(id);
        paymentRepository.delete(payment);
        return paymentMapper.toResponseDto(payment);
    }

    private Payment preCreateInit(final PaymentCreateDto paymentCreateDto) {
        paymentValidator.validateCreateDto(paymentCreateDto);
        return paymentMapper.toEntity(paymentCreateDto);
    }

}
