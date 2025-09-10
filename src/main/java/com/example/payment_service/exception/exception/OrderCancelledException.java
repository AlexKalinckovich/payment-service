package com.example.payment_service.exception.exception;

public class OrderCancelledException extends RuntimeException {
    public OrderCancelledException(String message) {
        super(message);
    }
}
