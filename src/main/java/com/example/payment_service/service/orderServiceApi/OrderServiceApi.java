package com.example.payment_service.service.orderServiceApi;

import com.example.payment_service.exception.exception.OrderNotFoundException;
import com.example.payment_service.model.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.naming.ServiceUnavailableException;
import java.math.BigDecimal;

@Slf4j
@Service
public class OrderServiceApi {

    private final WebClient orderServiceClient;

    @Autowired
    public OrderServiceApi(@Qualifier("order-service-client") WebClient orderServiceClient) {
        this.orderServiceClient = orderServiceClient;
    }

    public Mono<BigDecimal> getOrderTotal(final Long orderId) {
        return orderServiceClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/order/total/{orderId}")
                        .build(orderId))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new IllegalArgumentException("Order not found: " + orderId)))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServiceUnavailableException("Order service unavailable")))
                .bodyToMono(BigDecimal.class);
    }

    public Mono<Boolean> existsOrderById(final Long orderId) {
        return orderServiceClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/order/exists/{id}")
                        .build(orderId))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new OrderNotFoundException("Order not found")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServiceUnavailableException("order-service unavailable")))
                .bodyToMono(Boolean.class);
    }

    public Mono<OrderStatus> getOrderStatusById(final Long orderId){
        return orderServiceClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/order/orderStatus/{id}")
                        .build(orderId))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new OrderNotFoundException("Order not found")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServiceUnavailableException("order-service unavailable")))
                .bodyToMono(OrderStatus.class)
                .onErrorResume((throwable -> Mono.error(new IllegalArgumentException("Invalid order status"))));
    }
}

