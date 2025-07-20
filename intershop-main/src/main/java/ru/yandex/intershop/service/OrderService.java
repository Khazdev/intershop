package ru.yandex.intershop.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.intershop.model.Order;

public interface OrderService {
    Mono<Order> createOrderFromCart();

    Flux<Order> getAllOrders(Long userId);

    Mono<Order> getOrderById(Long id);
}
