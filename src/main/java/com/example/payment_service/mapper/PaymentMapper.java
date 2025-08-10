package com.example.payment_service.mapper;


import com.example.payment_service.dto.payment.PaymentCreateDto;
import com.example.payment_service.dto.payment.PaymentResponseDto;
import com.example.payment_service.dto.payment.PaymentUpdateDto;
import com.example.payment_service.model.Payment;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PaymentMapper {

    Payment toEntity(PaymentCreateDto dto);

    PaymentResponseDto toResponseDto(Payment payment);

    List<PaymentResponseDto> toResponseDtoList(List<Payment> payments);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(PaymentUpdateDto dto, @MappingTarget Payment payment);
}