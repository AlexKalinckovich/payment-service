package com.example.payment_service.mapper;

import com.example.payment_service.dto.payment.PaymentCreateDto;
import com.example.payment_service.dto.payment.PaymentResponseDto;
import com.example.payment_service.dto.payment.PaymentUpdateDto;
import com.example.payment_service.model.Payment;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "timeStamp", qualifiedByName = "setNowTimeStamp")
    })
    Payment toEntity(final PaymentCreateDto paymentCreateDto);

    PaymentResponseDto toResponseDto(final Payment payment);

    List<PaymentResponseDto> toResponseDtoList(final List<Payment> payments);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(final PaymentUpdateDto paymentUpdateDto, final Payment payment);

    @Named("setNowTimeStamp")
    default Payment setNowTimeStamp(final Payment payment){
        payment.setTimestamp(LocalDateTime.now());
        return payment;
    }

}
