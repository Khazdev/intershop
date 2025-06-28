package ru.yandex.intershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.intershop.enums.ActionType;
import ru.yandex.intershop.model.Cart;
import ru.yandex.intershop.model.CartItem;
import ru.yandex.intershop.model.Item;
import ru.yandex.intershop.repository.CartItemRepository;
import ru.yandex.intershop.repository.CartRepository;
import ru.yandex.intershop.repository.ItemRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ItemRepository itemRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    public Mono<Cart> getCurrentUserCart() {
        Long userId = 1L;
        return cartRepository.findByUserId(userId)
                .switchIfEmpty(Mono.defer(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                }))
                .flatMap(cart -> cartItemRepository.findByCartId(cart.getId())
                        .flatMap(cartItem -> itemRepository.findById(cartItem.getItemId())
                                .map(item -> {
                                    cartItem.setItem(item);
                                    return cartItem;
                                }))
                        .collectList()
                        .map(cartItems -> {
                            cart.setItems(cartItems);
                            return cart;
                        }));
    }

    @Override
    public Mono<Void> updateCartItem(Long itemId, ActionType action) {
        return getCurrentUserCart()
                .zipWith(itemRepository.findById(itemId)
                        .switchIfEmpty(Mono.error(new IllegalStateException("Item not found"))))
                .flatMap(tuple -> {
                    Cart cart = tuple.getT1();
                    Item item = tuple.getT2();
                    return cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())
                            .flatMap(cartItem -> handleCartItemUpdate(cart, cartItem, action))
                            .switchIfEmpty(handleNewCartItem(cart, item, action))
                            .then();
                });
    }

    private Mono<CartItem> handleCartItemUpdate(Cart cart, CartItem cartItem, ActionType action) {
        switch (action) {
            case PLUS:
                cartItem.setQuantity(cartItem.getQuantity() + 1);
                return cartItemRepository.save(cartItem);
            case MINUS:
                if (cartItem.getQuantity() > 1) {
                    cartItem.setQuantity(cartItem.getQuantity() - 1);
                    return cartItemRepository.save(cartItem);
                } else {
                    return cartItemRepository.delete(cartItem)
                            .then(Mono.fromRunnable(() -> cart.getItems().remove(cartItem)))
                            .thenReturn(cartItem);
                }
            case DELETE:
                return cartItemRepository.delete(cartItem)
                        .then(Mono.fromRunnable(() -> cart.getItems().remove(cartItem)))
                        .thenReturn(cartItem);
            default:
                return Mono.error(new IllegalArgumentException("Unknown action: " + action));
        }
    }

    private Mono<CartItem> handleNewCartItem(Cart cart, Item item, ActionType action) {
        if (action != ActionType.PLUS) {
            return Mono.empty();
        }
        CartItem newCartItem = new CartItem();
        newCartItem.setCartId(cart.getId());
        newCartItem.setItemId(item.getId());
        newCartItem.setQuantity(1);
        return cartItemRepository.save(newCartItem)
                .doOnNext(cartItem -> {
                    cartItem.setCart(cart);
                    cartItem.setItem(item);
                    cart.getItems().add(cartItem);
                });
    }

    @Override
    public Mono<BigDecimal> calculateTotal(Cart cart) {
        return Mono.just(cart.getItems().stream()
                .map(item -> item.getItem().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    @Override
    public void saveCart(Cart cart) {
        cartRepository.save(cart);
    }
}