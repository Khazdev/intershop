package ru.yandex.intershop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.intershop.enums.ActionType;
import ru.yandex.intershop.exception.ItemNotFoundException;
import ru.yandex.intershop.exception.UnknownActionException;
import ru.yandex.intershop.model.Cart;
import ru.yandex.intershop.model.CartItem;
import ru.yandex.intershop.model.Item;
import ru.yandex.intershop.repository.CartItemRepository;
import ru.yandex.intershop.repository.CartRepository;
import ru.yandex.intershop.repository.ItemRepository;

import java.math.BigDecimal;

@Slf4j
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
        log.debug("Updating cart item with itemId: {}, action: {}", itemId, action);
        return getCurrentUserCart()
                .zipWith(itemRepository.findById(itemId)
                        .switchIfEmpty(Mono.error(new ItemNotFoundException("Item not found"))))
                .flatMap(tuple -> {
                    Cart cart = tuple.getT1();
                    Item item = tuple.getT2();
                    log.debug("Found cart with ID: {}, item with ID: {}", cart.getId(), item.getId());
                    return cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())
                            .flatMap(existingItem -> {
                                log.debug("Found existing cart item with quantity: {}", existingItem.getQuantity());
                                return handleCartItemUpdate(cart, existingItem, action)
                                        .thenReturn(true);
                            })
                            .defaultIfEmpty(false)
                            .flatMap(found -> {
                                if (!found && action == ActionType.PLUS) {
                                    return handleNewCartItem(cart, item, action);
                                }
                                return Mono.empty();
                            });
                });
    }

    private Mono<Void> handleCartItemUpdate(Cart cart, CartItem cartItem, ActionType action) {
        switch (action) {
            case PLUS:
                cartItem.setQuantity(cartItem.getQuantity() + 1);
                return cartItemRepository.save(cartItem).then();
            case MINUS:
                if (cartItem.getQuantity() > 1) {
                    cartItem.setQuantity(cartItem.getQuantity() - 1);
                    return cartItemRepository.save(cartItem).then();
                } else {
                    return cartItemRepository.delete(cartItem)
                            .then(Mono.fromRunnable(() -> cart.getItems().remove(cartItem)));
                }
            case DELETE:
                return cartItemRepository.delete(cartItem)
                        .then(Mono.fromRunnable(() -> cart.getItems().remove(cartItem)));
            default:
                return Mono.error(new UnknownActionException("Unknown action: " + action));
        }
    }

    private Mono<Void> handleNewCartItem(Cart cart, Item item, ActionType action) {
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
                }).then();
    }

    @Override
    public Mono<BigDecimal> calculateTotal(Cart cart) {
        return Mono.just(cart.getItems().stream()
                .map(item -> item.getItem().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    @Override
    public Mono<Void> saveCart(Cart cart) {
        return cartItemRepository.deleteByCartId(cart.getId())
                .then(cartRepository.save(cart).then());
    }
}