package com.example.payment_service.validator;

import com.example.payment_service.dto.payment.PaymentCreateDto;
import com.example.payment_service.dto.payment.PaymentUpdateDto;
import com.example.payment_service.exception.PaymentNotFound;
import com.example.payment_service.model.Payment;
import com.example.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PaymentValidator implements CreateUpdateValidator<PaymentCreateDto, PaymentUpdateDto>{

    private final PaymentRepository paymentRepository;

    @Override
    public void validateCreateDto(final PaymentCreateDto dto) {

    }

    @Override
    public void validateUpdateDto(final PaymentUpdateDto dto) {

    }

    public Payment validatePaymentExistence(final String id){
        final Optional<Payment> paymentOptional = paymentRepository.findById(id);
        if(paymentOptional.isEmpty()){
            throw new PaymentNotFound(id);
        }
        return paymentOptional.get();
    }

    public List<Payment> validatedPaymentsExistenceByIds(final List<String> paymentIds) {
        final Set<String> uniqueIds = new HashSet<>(paymentIds);
        final List<Payment> payments = paymentRepository.findAllById(uniqueIds);
        if(payments.size() != uniqueIds.size()){
            final List<String> missingIds = new LinkedList<>();
            for(final Payment payment: payments){
                final String paymentId = payment.getId();
                if(!missingIds.contains(paymentId)){
                    missingIds.addLast(paymentId);
                }
            }
            throw new PaymentNotFound(missingIds);
        }

        return payments;
    }
}
