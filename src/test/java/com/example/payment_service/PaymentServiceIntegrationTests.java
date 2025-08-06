package com.example.payment_service;

import com.example.payment_service.dto.payment.PaymentCreateDto;
import com.example.payment_service.dto.payment.PaymentResponseDto;
import com.example.payment_service.dto.payment.PaymentUpdateDto;
import com.example.payment_service.dto.randomNumberApi.RandomNumberResponseDto;
import com.example.payment_service.exception.PaymentNotFound;
import com.example.payment_service.mapper.PaymentMapper;
import com.example.payment_service.model.Payment;
import com.example.payment_service.model.PaymentStatus;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.service.payment.PaymentService;
import com.example.payment_service.service.publishers.PaymentEventPublisher;
import com.example.payment_service.service.randomNumberApi.RandomNumberService;
import com.example.payment_service.validator.PaymentValidator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;

import javax.naming.ServiceUnavailableException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Testcontainers
@SpringBootTest
public class PaymentServiceIntegrationTests {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.3");

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.1"));

    @BeforeAll
    static void beforeAll() {
        mongoDBContainer.start();
        kafkaContainer.start();

        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        System.setProperty("spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers());

    }

    @AfterAll
    static void afterAll() {
        kafkaContainer.stop();
        mongoDBContainer.stop();
    }

    private PaymentCreateDto createDto;
    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final long USER_ID = 1L;
    private static final long ORDER_ID = 1L;


    @BeforeEach
    void setup() {
        createDto = new PaymentCreateDto(1L, 1L, NOW, new BigDecimal("123.45"));
    }

    @MockitoBean
    private RandomNumberService randomNumberService;

    @MockitoBean
    private PaymentValidator paymentValidator;

    @MockitoBean
    private PaymentMapper paymentMapper;

    @MockitoSpyBean
    private PaymentRepository paymentRepository;

    @MockitoSpyBean
    private PaymentEventPublisher paymentEventPublisher;

    @Autowired
    private PaymentService paymentService;

    @Test
    void createPayment_evenRandom_succeedsAndPublishesEvent() {
        final RandomNumberResponseDto rndDto = new RandomNumberResponseDto(List.of(42L));
        final Payment payment = Payment.builder()
                .status(PaymentStatus.SUCCESS)
                .timestamp(NOW)
                .userId(1L)
                .orderId(1L)
                .paymentAmount(BigDecimal.ONE)
                .build();
        when(paymentMapper.toEntity(any())).thenReturn(payment);
        when(randomNumberService.getRandomNumber(anyLong(), anyLong(), anyLong()))
                .thenReturn(Mono.just(rndDto));
        when(paymentRepository.save(payment)).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(paymentEventPublisher).publish(any(Payment.class));
        doNothing().when(paymentValidator).validateCreateDto(any());

        assertDoesNotThrow(() -> paymentService.createPayment(createDto));

        verify(paymentValidator).validateCreateDto(createDto);
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentEventPublisher).publish(any(Payment.class));
    }

    @Test
    void createPayment_oddRandom_succeedsWithFailedStatus() {
        final RandomNumberResponseDto rndDto = new RandomNumberResponseDto(List.of(41L));
        final Payment payment = Payment.builder()
                .status(PaymentStatus.SUCCESS)
                .timestamp(NOW)
                .userId(1L)
                .orderId(1L)
                .paymentAmount(BigDecimal.ONE)
                .build();
        when(randomNumberService.getRandomNumber(anyLong(), anyLong(), anyLong()))
                .thenReturn(Mono.just(rndDto));
        when(paymentMapper.toEntity(any())).thenReturn(payment);
        when(paymentRepository.save(payment)).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(paymentEventPublisher).publish(any(Payment.class));
        doNothing().when(paymentValidator).validateCreateDto(any());

        assertDoesNotThrow(() -> paymentService.createPayment(createDto));

        final ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertEquals(PaymentStatus.FAILED, captor.getValue().getStatus());
        verify(paymentEventPublisher).publish(any(Payment.class));
    }

    @Test
    void getPaymentById_found_returnsDto() {
        final Payment payment = new Payment();
        payment.setId("1");
        when(paymentRepository.findById("1")).thenReturn(Optional.of(payment));

        final PaymentResponseDto response = new PaymentResponseDto("1", 1L, 1L, PaymentStatus.SUCCESS, LocalDateTime.now(), BigDecimal.TEN);
        when(paymentMapper.toResponseDto(payment)).thenReturn(response);

        final PaymentResponseDto result = paymentService.getPaymentById("1");

        assertEquals(response, result);
        verify(paymentRepository).findById("1");
        verify(paymentMapper).toResponseDto(payment);
    }

    @Test
    void createPayment_randomServiceUnavailable_throwsServiceUnavailable() {
        when(randomNumberService.getRandomNumber(anyLong(), anyLong(), anyLong())).thenReturn(Mono.empty());
        doNothing().when(paymentValidator).validateCreateDto(any());

        final ServiceUnavailableException ex = assertThrows(ServiceUnavailableException.class,
                () -> paymentService.createPayment(createDto));
        assertTrue(ex.getMessage().contains("RandomApiService is unavailable"));
        verify(paymentRepository, never()).save(any());
        verify(paymentEventPublisher, never()).publish(any());
    }

    @Test
    void getPaymentById_notFound_throws() {
        when(paymentRepository.findById("InvalidId")).thenReturn(Optional.empty());

        assertThrows(PaymentNotFound.class, () -> paymentService.getPaymentById("InvalidId"));

        verify(paymentRepository).findById("InvalidId");
        verifyNoInteractions(paymentMapper);
    }

    @Test
    void getPaymentByOrderId_returnsMappedList() {
        final Payment payment = Payment.builder()
                .id("1")
                .paymentAmount(BigDecimal.ONE)
                .timestamp(NOW)
                .userId(USER_ID)
                .orderId(ORDER_ID)
                .status(PaymentStatus.SUCCESS)
                .build();

        final PaymentResponseDto dto = new PaymentResponseDto(
                "1", ORDER_ID, USER_ID, PaymentStatus.SUCCESS , NOW, BigDecimal.ONE);

        final List<Payment> payments = List.of(payment);
        when(paymentRepository.getPaymentByOrderId(1L)).thenReturn(payments);
        final List<PaymentResponseDto> dtos = List.of(dto);
        when(paymentMapper.toResponseDtoList(payments)).thenReturn(dtos);

        final List<PaymentResponseDto> result = paymentService.getPaymentByOrderId(1L);

        assertEquals(dtos, result);
        verify(paymentRepository).getPaymentByOrderId(1L);
        verify(paymentMapper).toResponseDtoList(payments);
    }

    @Test
    void getPaymentByUserId_returnsMappedList() {
        final Payment payment = Payment.builder()
                .id("1")
                .paymentAmount(BigDecimal.ONE)
                .timestamp(NOW)
                .userId(USER_ID)
                .orderId(ORDER_ID)
                .status(PaymentStatus.SUCCESS)
                .build();

        final PaymentResponseDto dto = new PaymentResponseDto(
                "1", ORDER_ID, USER_ID, PaymentStatus.SUCCESS , NOW, BigDecimal.ONE);

        final List<Payment> payments = List.of(payment);
        when(paymentRepository.getPaymentByUserId(1L)).thenReturn(payments);
        final List<PaymentResponseDto> dtos = List.of(dto);
        when(paymentMapper.toResponseDtoList(payments)).thenReturn(dtos);

        final List<PaymentResponseDto> result = paymentService.getPaymentByUserId(1L);

        assertEquals(dtos, result);
        verify(paymentRepository).getPaymentByUserId(1L);
        verify(paymentMapper).toResponseDtoList(payments);
    }

    @Test
    void updatePayment_success() {
        final PaymentUpdateDto updateDto = new PaymentUpdateDto();
        updateDto.setId("1");
        doNothing().when(paymentValidator).validateUpdateDto(updateDto);

        final Payment payment = new Payment();
        when(paymentValidator.validatePaymentExistence("1")).thenReturn(payment);

        doNothing().when(paymentMapper).updateEntity(updateDto, payment);

        final Payment savedPayment = new Payment();
        when(paymentRepository.save(payment)).thenReturn(savedPayment);

        final PaymentResponseDto response = new PaymentResponseDto(
                "1", ORDER_ID, USER_ID, PaymentStatus.SUCCESS , NOW, BigDecimal.ONE);

        when(paymentMapper.toResponseDto(savedPayment)).thenReturn(response);

        final PaymentResponseDto responseDto = paymentService.updatePayment(updateDto);

        assertEquals(response, responseDto);
        verify(paymentValidator).validateUpdateDto(updateDto);
        verify(paymentValidator).validatePaymentExistence("1");
        verify(paymentMapper).updateEntity(updateDto, payment);
        verify(paymentRepository).save(payment);
        verify(paymentMapper).toResponseDto(savedPayment);
    }

    @Test
    void deletePayment_success() {
        final Payment payment = new Payment();
        when(paymentValidator.validatePaymentExistence("1")).thenReturn(payment);

        doNothing().when(paymentRepository).delete(payment);

        final PaymentResponseDto response = new PaymentResponseDto(
                "1", ORDER_ID, USER_ID, PaymentStatus.SUCCESS , NOW, BigDecimal.ONE);
        when(paymentMapper.toResponseDto(payment)).thenReturn(response);

        final PaymentResponseDto result = paymentService.deletePayment("1");

        assertEquals(response, result);
        verify(paymentValidator).validatePaymentExistence("1");
        verify(paymentRepository).delete(payment);
        verify(paymentMapper).toResponseDto(payment);
    }


    @Test
    void getPaymentsByIds_returnsList() {
        final PaymentResponseDto first = new PaymentResponseDto(
                "1", ORDER_ID, USER_ID, PaymentStatus.SUCCESS , NOW, BigDecimal.ONE);
        final PaymentResponseDto second = new PaymentResponseDto(
                "1", ORDER_ID, USER_ID, PaymentStatus.SUCCESS , NOW, BigDecimal.ONE);

        final List<String> ids = List.of("1", "2", "3");
        final List<Payment> payments = List.of(new Payment(), new Payment());
        when(paymentValidator.validatedPaymentsExistenceByIds(ids)).thenReturn(payments);

        final List<PaymentResponseDto> dtos = List.of(first, second);
        when(paymentMapper.toResponseDtoList(payments)).thenReturn(dtos);

        final List<PaymentResponseDto> result = paymentService.getPaymentsByIds(ids);

        assertEquals(dtos, result);
        verify(paymentValidator).validatedPaymentsExistenceByIds(ids);
        verify(paymentMapper).toResponseDtoList(payments);
    }


}