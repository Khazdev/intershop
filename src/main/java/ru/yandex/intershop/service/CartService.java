package ru.yandex.intershop.service;

import reactor.core.publisher.Mono;
import ru.yandex.intershop.enums.ActionType;
import ru.yandex.intershop.model.Cart;

import java.math.BigDecimal;

public interface CartService {
    void updateCartItem(Long itemId, ActionType action);
    Mono<Cart> getCurrentUserCart();
    BigDecimal calculateTotal(Cart cart);
    void saveCart(Cart cart);
}
