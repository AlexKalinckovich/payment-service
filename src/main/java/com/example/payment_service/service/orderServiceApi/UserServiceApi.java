package com.example.payment_service.service.orderServiceApi;

import com.example.payment_service.exception.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.naming.ServiceUnavailableException;

@Service
public class UserServiceApi {

    private final WebClient userServiceClient;

    public UserServiceApi(@Qualifier("user-service-client") WebClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    public Mono<Boolean> existsUserById(final Long userId){
        return userServiceClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/user/exists/{userId}")
                        .build(userId))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        (e) -> Mono.error(new UserNotFoundException("User not found"))
                )
                .onStatus(HttpStatusCode::is5xxServerError,
                        (e) -> Mono.error(new ServiceUnavailableException("User-service not found")))
                .bodyToMono(Boolean.class);
    }
}
