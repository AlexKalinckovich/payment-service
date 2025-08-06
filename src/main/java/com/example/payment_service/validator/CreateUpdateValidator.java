package com.example.payment_service.validator;

public interface CreateUpdateValidator<C, U>{

    void validateCreateDto(C dto);

    void validateUpdateDto(U dto);

}
