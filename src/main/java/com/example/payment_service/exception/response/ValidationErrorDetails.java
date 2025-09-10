package com.example.payment_service.exception.response;

import java.util.Map;

public record ValidationErrorDetails(Map<String, String> fieldErrors) implements ErrorDetails {
}
