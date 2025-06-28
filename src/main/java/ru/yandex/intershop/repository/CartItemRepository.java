package ru.yandex.intershop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import ru.yandex.intershop.model.Cart;
import ru.yandex.intershop.model.CartItem;
import ru.yandex.intershop.model.Item;

import java.util.Optional;

public interface CartItemRepository extends R2dbcRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndItem(Cart cart, Item item);
    Flux<CartItem> findByCartId(Long cartId);
}