package com.example.payment_service.service.randomNumberApi;

import reactor.core.publisher.Mono;

import java.util.List;

public interface RandomNumberService {
    Mono<List<Long>> getRandomNumber(long min, long max, long count);
}
