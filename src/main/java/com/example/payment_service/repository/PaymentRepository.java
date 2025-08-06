package com.example.payment_service.repository;

import com.example.payment_service.model.Payment;
import com.example.payment_service.model.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends MongoRepository<Payment,String> {

    List<Payment> getPaymentByOrderId(Long orderId);

    List<Payment> getPaymentByUserId(Long userId);

    List<Payment> getPaymentByStatus(PaymentStatus status);

    @Query(value = "{}", fields = "{ total: { $sum: \"$paymentAmount\" } }")
    BigDecimal getTotalAmount();

}
