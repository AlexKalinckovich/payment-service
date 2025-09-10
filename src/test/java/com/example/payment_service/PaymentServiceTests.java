package com.example.payment_service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.payment_service.dto.payment.PaymentCreateDto;
import com.example.payment_service.dto.payment.PaymentResponseDto;
import com.example.payment_service.exception.exception.PaymentNotFoundException;
import com.example.payment_service.mapper.PaymentMapper;
import com.example.payment_service.model.Payment;
import com.example.payment_service.model.PaymentStatus;
import com.example.payment_service.repository.PaymentRepository;

import com.example.payment_service.service.payment.PaymentServiceImpl;
import com.example.payment_service.service.publishers.PaymentEventPublisher;
import com.example.payment_service.service.randomNumberApi.RandomNumberService;
import com.example.payment_service.validator.PaymentValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import javax.naming.ServiceUnavailableException;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTests {

    @Mock
    private RandomNumberService randomNumberService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentValidator paymentValidator;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentCreateDto createDto;
    private Payment entity;
    private Payment savedEntity;
    private PaymentResponseDto responseDto;

    private static final LocalDateTime NOW = LocalDateTime.now();

    @BeforeEach
    void setup() {
        createDto = new PaymentCreateDto(1L, 1L, NOW, new BigDecimal("123.45"));

        entity = new Payment();
        entity.setOrderId(createDto.getOrderId());
        entity.setUserId(createDto.getUserId());
        entity.setPaymentAmount(createDto.getPaymentAmount());

        savedEntity = new Payment();
        savedEntity.setId("1");
        savedEntity.setOrderId(createDto.getOrderId());
        savedEntity.setUserId(createDto.getUserId());
        savedEntity.setPaymentAmount(createDto.getPaymentAmount());
        savedEntity.setTimestamp(NOW);
        savedEntity.setStatus(PaymentStatus.SUCCESS);

        responseDto = new PaymentResponseDto(
                "1",
                savedEntity.getOrderId(),
                savedEntity.getUserId(),
                PaymentStatus.SUCCESS,
                NOW,
                savedEntity.getPaymentAmount()
        );

    }

    @Test
    void createPayment_evenRandom_succeedsAndPublishesEvent() {
        when(paymentValidator.validateCreateDto(createDto)).thenReturn(entity);
        when(paymentMapper.toEntity(createDto)).thenReturn(entity);

        final List<Long> rndDto = List.of(42L);
        when(randomNumberService.getRandomNumber(anyLong(), anyLong(), anyLong()))
                .thenReturn(Mono.just(rndDto));
        when(paymentRepository.save(entity)).thenReturn(savedEntity);

        assertDoesNotThrow(() -> paymentService.createPayment(createDto));

        assertEquals(PaymentStatus.SUCCESS, entity.getStatus());

        verify(paymentValidator).validateCreateDto(createDto);
        verify(paymentRepository).save(entity);
        assertDoesNotThrow(() -> paymentEventPublisher.publish(savedEntity));
    }

    @Test
    void createPayment_oddRandom_succeedsWithFailedStatus() {
        when(paymentMapper.toEntity(createDto)).thenReturn(entity);
        when(paymentValidator.validateCreateDto(any())).thenReturn(savedEntity);

        final List<Long> rndDto = List.of(42L);
        when(randomNumberService.getRandomNumber(anyLong(), anyLong(), anyLong()))
                .thenReturn(Mono.just(rndDto));
        when(paymentRepository.save(entity)).thenReturn(savedEntity);

        assertDoesNotThrow(() -> paymentService.createPayment(createDto));

        assertEquals(PaymentStatus.SUCCESS, entity.getStatus());

        verify(paymentValidator).validateCreateDto(createDto);
        verify(paymentRepository).save(entity);
        assertDoesNotThrow(() -> paymentEventPublisher.publish(savedEntity));
    }

    @Test
    void createPayment_randomServiceUnavailable_throwsException() {
        when(paymentValidator.validateCreateDto(any())).thenReturn(entity);

        when(randomNumberService.getRandomNumber(anyLong(), anyLong(), anyLong()))
                .thenReturn(Mono.empty());

        final ServiceUnavailableException ex = assertThrows(ServiceUnavailableException.class,
                () -> paymentService.createPayment(createDto));

        assertTrue(ex.getMessage().contains("Random service timeout"));

        verify(paymentRepository, never()).save(any());
        assertDoesNotThrow(() -> paymentEventPublisher.publish(savedEntity));
    }

    @Test
    void getPaymentById_found_returnsDto() {
        when(paymentRepository.findById("1")).thenReturn(Optional.of(savedEntity));
        when(paymentMapper.toResponseDto(savedEntity)).thenReturn(responseDto);

        final PaymentResponseDto result = paymentService.getPaymentById("1");

        assertEquals(responseDto, result);
        verify(paymentRepository).findById("1");
        verify(paymentMapper).toResponseDto(savedEntity);
    }

    @Test
    void getPaymentById_notFound_throwsPaymentNotFound() {
        when(paymentRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.getPaymentById("1"));

        verify(paymentRepository).findById("1");
        verifyNoMoreInteractions(paymentMapper);
    }

    @Test
    void getPaymentByOrderId_returnsMappedList() {
        final List<Payment> payments = List.of(savedEntity);
        final List<PaymentResponseDto> dtoList = List.of(responseDto);

        when(paymentRepository.getPaymentByOrderId(createDto.getOrderId())).thenReturn(payments);
        when(paymentMapper.toResponseDtoList(payments)).thenReturn(dtoList);

        final List<PaymentResponseDto> actual = paymentService.getPaymentByOrderId(createDto.getOrderId());

        assertEquals(dtoList, actual);
        verify(paymentRepository).getPaymentByOrderId(createDto.getOrderId());
        verify(paymentMapper).toResponseDtoList(payments);
    }

    @Test
    void getPaymentByUserId_returnsMappedList() {
        final List<Payment> payments = List.of(savedEntity);
        final List<PaymentResponseDto> dtoList = List.of(responseDto);

        when(paymentRepository.getPaymentByUserId(createDto.getUserId())).thenReturn(payments);
        when(paymentMapper.toResponseDtoList(payments)).thenReturn(dtoList);

        final List<PaymentResponseDto> actual = paymentService.getPaymentByUserId(createDto.getUserId());

        assertEquals(dtoList, actual);
        verify(paymentRepository).getPaymentByUserId(createDto.getUserId());
        verify(paymentMapper).toResponseDtoList(payments);
    }
}


