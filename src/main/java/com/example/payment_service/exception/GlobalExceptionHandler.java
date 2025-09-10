package com.example.payment_service.exception;


import com.example.payment_service.exception.exception.InsufficientFundsException;
import com.example.payment_service.exception.exception.InvalidPaymentMethodException;
import com.example.payment_service.exception.exception.OrderAlreadyPaidException;
import com.example.payment_service.exception.exception.OrderNotFoundException;
import com.example.payment_service.exception.exception.PaymentAlreadyExistsException;
import com.example.payment_service.exception.exception.PaymentDeclinedException;
import com.example.payment_service.exception.exception.PaymentNotFoundException;
import com.example.payment_service.exception.exception.PaymentProcessingException;
import com.example.payment_service.exception.exception.UserNotFoundException;
import com.example.payment_service.exception.response.ErrorResponse;
import com.example.payment_service.exception.response.ExceptionResponseService;
import com.example.payment_service.exception.response.ValidationErrorDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.MongoException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthenticationException;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.naming.ServiceUnavailableException;
import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ExceptionResponseService exceptionResponseService;

    /* =========================
       PAYMENT DOMAIN EXCEPTIONS
       ========================= */

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(PaymentNotFoundException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.NOT_FOUND, ErrorMessage.PAYMENT_NOT_FOUND
        );
    }

    @ExceptionHandler(PaymentAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlePaymentAlreadyExists(PaymentAlreadyExistsException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.CONFLICT, ErrorMessage.PAYMENT_ALREADY_EXISTS
        );
    }

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ErrorResponse> handlePaymentProcessing(PaymentProcessingException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessage.PAYMENT_PROCESSING_ERROR
        );
    }

    @ExceptionHandler(PaymentDeclinedException.class)
    public ResponseEntity<ErrorResponse> handlePaymentDeclined(PaymentDeclinedException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.BAD_REQUEST, ErrorMessage.PAYMENT_DECLINED
        );
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.BAD_REQUEST, ErrorMessage.INSUFFICIENT_FUNDS
        );
    }

    @ExceptionHandler(InvalidPaymentMethodException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPaymentMethod(InvalidPaymentMethodException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.BAD_REQUEST, ErrorMessage.INVALID_PAYMENT_METHOD
        );
    }

    /* =========================
       ORDER DOMAIN EXCEPTIONS
       ========================= */

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.NOT_FOUND, ErrorMessage.ORDER_NOT_FOUND
        );
    }

    @ExceptionHandler(OrderAlreadyPaidException.class)
    public ResponseEntity<ErrorResponse> handleOrderAlreadyPaid(OrderAlreadyPaidException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.CONFLICT, ErrorMessage.ORDER_ALREADY_PAID
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex,request, HttpStatus.NOT_FOUND, ErrorMessage.USER_NOT_FOUND
        );
    }

    /* =========================
       VALIDATION EXCEPTIONS
       ========================= */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        final Map<String, String> fieldErrorsMap = new LinkedHashMap<>();
        for (final FieldError fe : ex.getBindingResult().getFieldErrors()) {
            final String defaultMessage = fe.getDefaultMessage() == null ? "" : fe.getDefaultMessage();
            fieldErrorsMap.putIfAbsent(fe.getField(), defaultMessage);
        }

        final ValidationErrorDetails details = new ValidationErrorDetails(fieldErrorsMap);

        final ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                ErrorMessage.VALIDATION_ERROR.name(),
                "Payment request validation failed",
                request.getDescription(false),
                details
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        final Map<String, String> fieldErrorsMap = new LinkedHashMap<>();
        for (final ConstraintViolation<?> cv : ex.getConstraintViolations()) {
            final String defaultMessage = cv.getMessage() == null ? "" : cv.getMessage();
            fieldErrorsMap.putIfAbsent(cv.getPropertyPath().toString(), defaultMessage);
        }

        final ValidationErrorDetails details = new ValidationErrorDetails(fieldErrorsMap);

        final ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                ErrorMessage.VALIDATION_ERROR.name(),
                "Payment constraint validation failed",
                request.getDescription(false),
                details
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.BAD_REQUEST, ErrorMessage.INVALID_REQUEST
        );
    }

    /* =========================
       DATABASE EXCEPTIONS (MongoDB)
       ========================= */

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccess(DataAccessException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.SERVICE_UNAVAILABLE, ErrorMessage.DATABASE_ERROR
        );
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateKey(DuplicateKeyException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.CONFLICT, ErrorMessage.DUPLICATE_KEY_ERROR
        );
    }

    @ExceptionHandler(MongoException.class)
    public ResponseEntity<ErrorResponse> handleMongoException(MongoException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.SERVICE_UNAVAILABLE, ErrorMessage.DATABASE_CONNECTION_ERROR
        );
    }

    /* =========================
       KAFKA EXCEPTIONS
       ========================= */

    @ExceptionHandler(KafkaException.class)
    public ResponseEntity<ErrorResponse> handleKafkaException(KafkaException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.SERVICE_UNAVAILABLE, ErrorMessage.KAFKA_PRODUCER_ERROR
        );
    }

    @ExceptionHandler(SerializationException.class)
    public ResponseEntity<ErrorResponse> handleKafkaSerialization(SerializationException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessage.KAFKA_SERIALIZATION_ERROR
        );
    }

    @ExceptionHandler(DeserializationException.class)
    public ResponseEntity<ErrorResponse> handleKafkaDeserialization(DeserializationException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessage.KAFKA_DESERIALIZATION_ERROR
        );
    }

    /* =========================
       WEB CLIENT EXCEPTIONS
       ========================= */

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleWebClientResponse(WebClientResponseException ex, WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        if (status.is4xxClientError()) {
            return exceptionResponseService.buildErrorResponse(
                    ex, request, status, ErrorMessage.EXTERNAL_SERVICE_ERROR
            );
        } else {
            return exceptionResponseService.buildErrorResponse(
                    ex, request, status, ErrorMessage.EXTERNAL_SERVICE_UNAVAILABLE
            );
        }
    }

    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ErrorResponse> handleWebClientRequest(WebClientRequestException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.SERVICE_UNAVAILABLE, ErrorMessage.EXTERNAL_SERVICE_TIMEOUT
        );
    }

    /* =========================
       JSON PROCESSING EXCEPTIONS
       ========================= */

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ErrorResponse> handleJsonProcessing(JsonProcessingException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.BAD_REQUEST, ErrorMessage.JSON_PARSING_ERROR
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.BAD_REQUEST, ErrorMessage.JSON_PARSING_ERROR
        );
    }

    /* =========================
       AUTHENTICATION/AUTHORIZATION
       ========================= */

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.UNAUTHORIZED, ErrorMessage.AUTHENTICATION_ERROR
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.FORBIDDEN, ErrorMessage.AUTHORIZATION_ERROR
        );
    }

    /* =========================
       FALLBACK EXCEPTIONS
       ========================= */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessage.SERVER_ERROR
        );
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailable(ServiceUnavailableException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.SERVICE_UNAVAILABLE, ErrorMessage.SERVICE_UNAVAILABLE
        );
    }
}
