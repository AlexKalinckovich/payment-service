package com.example.payment_service.exception.exception;

public class OrderCompleteException extends RuntimeException {
    public OrderCompleteException(String message) {
        super(message);
    }
}
