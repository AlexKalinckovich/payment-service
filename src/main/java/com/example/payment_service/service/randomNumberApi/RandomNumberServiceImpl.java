package com.example.payment_service.service.randomNumberApi;

import com.example.payment_service.dto.randomNumberApi.RandomNumberResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Qualifier("randomNumberServiceApi")
@Service
public class RandomNumberServiceImpl implements RandomNumberService {

    private final WebClient randomApiClient;

    @Autowired
    public RandomNumberServiceImpl(@Qualifier("randomApiClient") final WebClient randomApiClient) {
        this.randomApiClient = randomApiClient;
    }

    @Override
    public Mono<RandomNumberResponseDto> getRandomNumber(final long min,
                                                         final long max,
                                                         final long count) {
        return randomApiClient.get()
                .uri("/?min={min}&max={max}&count={count}", min, max, count)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(RandomNumberResponseDto.class);
    }

}
