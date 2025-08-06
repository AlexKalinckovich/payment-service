package com.example.payment_service.service.payment;

import com.example.payment_service.dto.payment.PaymentCreateDto;
import com.example.payment_service.dto.payment.PaymentResponseDto;
import com.example.payment_service.dto.payment.PaymentUpdateDto;
import com.example.payment_service.dto.randomNumberApi.RandomNumberResponseDto;
import com.example.payment_service.exception.PaymentNotFound;
import com.example.payment_service.mapper.PaymentMapper;
import com.example.payment_service.model.Payment;
import com.example.payment_service.model.PaymentStatus;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.service.publishers.PaymentEventPublisher;
import com.example.payment_service.service.randomNumberApi.RandomNumberService;
import com.example.payment_service.validator.PaymentValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.naming.ServiceUnavailableException;
import java.util.List;
import java.util.Optional;

@Service
@Qualifier("paymentServiceImpl")
public class PaymentServiceImpl implements PaymentService {

    private static final int MIN_RANDOM_VALUE = 1;
    private static final int MAX_RANDOM_VALUE = 2000;

    private final RandomNumberService randomNumberService;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentValidator paymentValidator;
    private final PaymentEventPublisher paymentEventPublisher;

    @Autowired
    public PaymentServiceImpl(@Qualifier("randomNumberServiceApi") final RandomNumberService randomNumberService,
                              final PaymentRepository paymentRepository,
                              final PaymentMapper paymentMapper,
                              final PaymentValidator paymentValidator,
                              final PaymentEventPublisher paymentEventPublisher) {
        this.randomNumberService = randomNumberService;
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.paymentValidator = paymentValidator;
        this.paymentEventPublisher = paymentEventPublisher;
    }


    @Override
    public void createPayment(final PaymentCreateDto paymentCreateDto) throws ServiceUnavailableException {
        paymentValidator.validateCreateDto(paymentCreateDto);

        final Payment entity = paymentMapper.toEntity(paymentCreateDto);

        final RandomNumberResponseDto randomDto = randomNumberService
                .getRandomNumber(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE, 1)
                .block();

        if (randomDto == null) {
            throw new ServiceUnavailableException("RandomApiService is unavailable");
        }

        final long n = randomDto.numbers().getFirst();
        entity.setStatus(n % 2 == 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        final Payment saved = paymentRepository.save(entity);

        paymentEventPublisher.publish(saved);

        paymentMapper.toResponseDto(saved);
    }

    @Override
    public PaymentResponseDto getPaymentById(final String id) {
        final Optional<Payment> payment = paymentRepository.findById(id);
        if(payment.isEmpty()){
            throw new PaymentNotFound(id);
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
        paymentValidator.validateUpdateDto(paymentUpdateDto);
        final Payment payment = paymentValidator.validatePaymentExistence(paymentUpdateDto.getId());
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
}
