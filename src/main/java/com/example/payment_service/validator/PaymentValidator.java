package com.example.payment_service.validator;

import com.example.payment_service.dto.payment.PaymentCreateDto;
import com.example.payment_service.dto.payment.PaymentUpdateDto;
import com.example.payment_service.exception.exception.OrderAlreadyPaidException;
import com.example.payment_service.exception.exception.OrderCancelledException;
import com.example.payment_service.exception.exception.OrderCompleteException;
import com.example.payment_service.exception.exception.OrderNotFoundException;
import com.example.payment_service.exception.exception.OrderProcessingException;
import com.example.payment_service.exception.exception.PaymentNotFoundException;
import com.example.payment_service.exception.exception.UserNotFoundException;
import com.example.payment_service.mapper.PaymentMapper;
import com.example.payment_service.model.OrderStatus;
import com.example.payment_service.model.Payment;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.service.orderServiceApi.OrderServiceApi;
import com.example.payment_service.service.orderServiceApi.UserServiceApi;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PaymentValidator implements CreateUpdateValidator<PaymentCreateDto, PaymentUpdateDto, Payment>{

    private static final Map<OrderStatus, RuntimeException> errors = Map.of(
        OrderStatus.PAID,       new OrderAlreadyPaidException("Order already paid"),
        OrderStatus.CANCELED,   new OrderCancelledException("Order cancelled"),
        OrderStatus.PROCESSING, new OrderProcessingException("Order processing"),
        OrderStatus.COMPLETED,  new OrderCompleteException("Order complete")
    );

    private final PaymentRepository paymentRepository;
    private final OrderServiceApi  orderServiceApi;
    private final UserServiceApi userServiceApi;
    private final PaymentMapper paymentMapper;



    @Override
    public Payment validateCreateDto(final PaymentCreateDto dto) {
        final Long orderId = dto.getOrderId();
        final Long userId = dto.getUserId();
        validateExistence(orderId, userId);

        return paymentMapper.toEntity(dto);
    }

    @Override
    public Payment validateUpdateDto(@NonNull final PaymentUpdateDto dto) {
        final Long orderId = dto.getOrderId();
        final Long userId = dto.getUserId();
        validateExistence(orderId, userId);
        validateOrderStatus(orderId);
        return paymentRepository.findById(dto.getId())
                .orElseThrow(() -> new PaymentNotFoundException(dto.getId()));
    }

    private void validateOrderStatus(final Long orderId) {
        final OrderStatus status = orderServiceApi.getOrderStatusById(orderId).block();
        if(status != OrderStatus.UNPAID){
            if(errors.containsKey(status)){
                throw errors.get(status);
            }else{
                throw new IllegalArgumentException("Cannot handle this OrderStatus" + OrderStatus.UNPAID);
            }
        }
    }

    public void validateExistence(final Long orderId, final Long userId){

        final boolean isOrderExists = Boolean.TRUE.equals(orderServiceApi.existsOrderById(orderId).block());

        if(!isOrderExists){
            throw new OrderNotFoundException("Order not found");
        }

        final boolean isUserExists = Boolean.TRUE.equals(userServiceApi.existsUserById(userId).block());
        if(!isUserExists){
            throw new UserNotFoundException("user-id not found");
        }
    }

    public Payment validatePaymentExistence(final String id){
        final Optional<Payment> paymentOptional = paymentRepository.findById(id);
        if(paymentOptional.isEmpty()){
            throw new PaymentNotFoundException(id);
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
            throw new PaymentNotFoundException(missingIds);
        }

        return payments;
    }

}
