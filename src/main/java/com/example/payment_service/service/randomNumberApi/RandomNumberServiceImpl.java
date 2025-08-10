package com.example.payment_service.service.randomNumberApi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Qualifier("randomNumberServiceApi")
@Service
public class RandomNumberServiceImpl implements RandomNumberService {

    private final WebClient randomApiClient;

    @Autowired
    public RandomNumberServiceImpl(@Qualifier("randomApiClient") final WebClient randomApiClient) {
        this.randomApiClient = randomApiClient;
    }

    public Mono<List<Long>> getRandomNumber(long min, long max, long count) {
        return randomApiClient.get()
                .uri("/?min={min}&max={max}&count={count}", min, max, count)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

}
