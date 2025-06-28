package ru.yandex.intershop.service;

import reactor.core.publisher.Mono;
import ru.yandex.intershop.enums.ActionType;
import ru.yandex.intershop.model.Cart;

import java.math.BigDecimal;

public interface CartService {
    Mono<Void> updateCartItem(Long itemId, ActionType action);
    Mono<Cart> getCurrentUserCart();
    Mono<BigDecimal> calculateTotal(Cart cart);
    void saveCart(Cart cart);
}
