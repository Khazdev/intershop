package ru.yandex.intershop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.intershop.model.CartItem;

public interface CartItemRepository extends R2dbcRepository<CartItem, Long> {
    Mono<CartItem> findByCartIdAndItemId(Long cartId, Long itemId);

    Flux<CartItem> findByCartId(Long cartId);
}