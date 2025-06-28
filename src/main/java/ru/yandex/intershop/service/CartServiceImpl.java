package ru.yandex.intershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.intershop.enums.ActionType;
import ru.yandex.intershop.model.Cart;
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
    public void updateCartItem(Long itemId, ActionType action) {
//        Cart cart = getCurrentUserCart();
//        Item item = itemRepository.findById(itemId)
//                .orElseThrow(() -> new EntityNotFoundException("Item not found"));
//
//        Optional<CartItem> existingItem = cartItemRepository.findByCartAndItem(cart, item);
//
//        switch (action) {
//            case PLUS -> {
//                if (existingItem.isPresent()) {
//                    CartItem cartItem = existingItem.get();
//                    cartItem.setQuantity(cartItem.getQuantity() + 1);
//                    cartItemRepository.save(cartItem);
//                } else {
//                    CartItem newCartItem = new CartItem();
//                    newCartItem.setCart(cart);
//                    newCartItem.setItem(item);
//                    newCartItem.setQuantity(1);
//                    cartItemRepository.save(newCartItem);
//                    cart.getItems().add(newCartItem);
//                }
//            }
//            case MINUS -> {
//                if (existingItem.isPresent()) {
//                    CartItem cartItem = existingItem.get();
//                    if (cartItem.getQuantity() > 1) {
//                        cartItem.setQuantity(cartItem.getQuantity() - 1);
//                        cartItemRepository.save(cartItem);
//                    } else {
//                        cartItemRepository.delete(cartItem);
//                        cart.getItems().remove(cartItem);
//                    }
//                }
//            }
//            case DELETE -> {
//                existingItem.ifPresent(cartItem -> {
//                    cartItemRepository.delete(cartItem);
//                    cart.getItems().remove(cartItem);
//                });
    }

    @Override
    public BigDecimal calculateTotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getItem().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public void saveCart(Cart cart) {
        cartRepository.save(cart);
    }
}