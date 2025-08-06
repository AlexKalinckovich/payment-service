package com.example.payment_service.exception;

import java.util.List;

public class PaymentNotFound extends RuntimeException{
    public PaymentNotFound(String id){
        super("Payment:" + id + " not found");
    }

    public PaymentNotFound(List<String> missingIds) {
        super("Payment:" + missingIds + " not found");
    }
}
