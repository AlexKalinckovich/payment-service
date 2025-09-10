package com.example.payment_service.validator;

public interface CreateUpdateValidator<C, U, R>{

    R validateCreateDto(C dto);

    R validateUpdateDto(U dto);

}
