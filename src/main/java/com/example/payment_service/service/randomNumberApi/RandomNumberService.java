package com.example.payment_service.service.randomNumberApi;

import com.example.payment_service.dto.randomNumberApi.RandomNumberResponseDto;
import reactor.core.publisher.Mono;

public interface RandomNumberService {
    Mono<RandomNumberResponseDto> getRandomNumber(long min, long max, long count);
}
