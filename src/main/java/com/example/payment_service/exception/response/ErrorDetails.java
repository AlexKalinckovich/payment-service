package com.example.payment_service.exception.response;

sealed interface ErrorDetails permits ValidationErrorDetails, SimpleErrorDetails {
}
