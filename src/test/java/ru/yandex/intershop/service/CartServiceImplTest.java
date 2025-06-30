package ru.yandex.intershop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.intershop.enums.ActionType;
import ru.yandex.intershop.model.Cart;
import ru.yandex.intershop.model.CartItem;
import ru.yandex.intershop.model.Item;
import ru.yandex.intershop.repository.CartItemRepository;
import ru.yandex.intershop.repository.CartRepository;
import ru.yandex.intershop.repository.ItemRepository;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart cart;
    private Item item;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.setId(1L);
        cart.setUserId(1L);
        cart.setItems(new ArrayList<>());

        item = new Item();
        item.setId(1L);
        item.setPrice(new BigDecimal("10.00"));

        cartItem = new CartItem();
        cartItem.setCartId(cart.getId());
        cartItem.setItemId(item.getId());
        cartItem.setItem(item);
        cartItem.setQuantity(1);
    }

    @Test
    void getCurrentUserCart_existingCart_returnsCart() {
        when(cartRepository.findByUserId(1L)).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(Flux.empty());

        Mono<Cart> result = cartService.getCurrentUserCart();

        StepVerifier.create(result)
                .expectNextMatches(c -> c.getUserId().equals(1L) && c.getItems().isEmpty())
                .verifyComplete();

        verify(cartRepository).findByUserId(1L);
        verify(cartItemRepository).findByCartId(cart.getId());
    }

    @Test
    void getCurrentUserCart_noCart_createsNewCart() {
        when(cartRepository.findByUserId(1L)).thenReturn(Mono.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(Flux.empty());

        Mono<Cart> result = cartService.getCurrentUserCart();

        StepVerifier.create(result)
                .expectNextMatches(c -> c.getUserId().equals(1L) && c.getItems().isEmpty())
                .verifyComplete();

        verify(cartRepository).findByUserId(1L);
        verify(cartRepository).save(any(Cart.class));
        verify(cartItemRepository).findByCartId(cart.getId());
    }

    @Test
    void updateCartItem_plusNewItem_addsToCart() {
        when(cartRepository.findByUserId(1L)).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(Flux.empty());
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));
        when(cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())).thenReturn(Mono.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(Mono.just(cartItem));

        Mono<Void> result = cartService.updateCartItem(1L, ActionType.PLUS);

        StepVerifier.create(result)
                .verifyComplete();

        StepVerifier.create(Mono.just(cart))
                .expectNextMatches(c -> c.getItems().size() == 1 && c.getItems().get(0).getQuantity() == 1)
                .verifyComplete();

        verify(cartItemRepository).findByCartId(cart.getId());
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void updateCartItem_plusExistingItem_incrementsQuantity() {
        cart.getItems().add(cartItem);
        cartItem.setQuantity(1);
        when(cartRepository.findByUserId(1L)).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(Flux.just(cartItem));
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));
        when(cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())).thenReturn(Mono.just(cartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem savedItem = invocation.getArgument(0);
            if (savedItem.getQuantity() == 2 && savedItem.getItem() != null) {
                cartItem.setQuantity(2);
                return Mono.just(cartItem);
            }
            return Mono.just(savedItem);
        });

        Mono<Void> result = cartService.updateCartItem(1L, ActionType.PLUS);

        StepVerifier.create(result)
                .verifyComplete();

        StepVerifier.create(Mono.just(cart))
                .expectNextMatches(c -> c.getItems().get(0).getQuantity() == 2)
                .verifyComplete();

        verify(cartItemRepository).findByCartId(cart.getId());
        verify(cartItemRepository).findByCartIdAndItemId(cart.getId(), item.getId());
        verify(cartItemRepository, times(1)).save(argThat(ci -> ci.getQuantity() == 2 && ci.getItem() != null));
    }

    @Test
    void updateCartItem_minusSingleItem_removesItem() {
        cart.getItems().add(cartItem);
        when(cartRepository.findByUserId(1L)).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(Flux.just(cartItem));
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));
        when(cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())).thenReturn(Mono.just(cartItem));
        when(cartItemRepository.delete(cartItem)).thenReturn(Mono.empty());

        Mono<Void> result = cartService.updateCartItem(1L, ActionType.MINUS);

        StepVerifier.create(result)
                .verifyComplete();

        StepVerifier.create(Mono.just(cart))
                .expectNextMatches(c -> c.getItems().isEmpty())
                .verifyComplete();

        verify(cartItemRepository).findByCartId(cart.getId());
        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void calculateTotal_withItems_returnsCorrectTotal() {
        cart.getItems().add(cartItem);

        Mono<BigDecimal> result = cartService.calculateTotal(cart);

        StepVerifier.create(result)
                .expectNext(new BigDecimal("10.00"))
                .verifyComplete();
    }
}