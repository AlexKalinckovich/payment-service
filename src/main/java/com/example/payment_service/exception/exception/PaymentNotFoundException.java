package com.example.payment_service.exception.exception;

import java.util.List;

public class PaymentNotFoundException extends RuntimeException{
    public PaymentNotFoundException(String id){
        super("Payment:" + id + " not found");
    }

    public PaymentNotFoundException(List<String> missingIds) {
        super("Payment:" + missingIds + " not found");
    }
}
