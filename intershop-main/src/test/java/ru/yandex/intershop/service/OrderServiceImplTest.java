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
import ru.yandex.intershop.exception.EmptyCartException;
import ru.yandex.intershop.exception.OrderNotFoundException;
import ru.yandex.intershop.model.Cart;
import ru.yandex.intershop.model.CartItem;
import ru.yandex.intershop.model.Item;
import ru.yandex.intershop.model.Order;
import ru.yandex.intershop.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderServiceImpl orderService;

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
        cartItem.setQuantity(2);
        cart.getItems().add(cartItem);
    }

    @Test
    void createOrderFromCart_validCart_createsOrder() {
        when(cartService.getCurrentUserCart()).thenReturn(Mono.just(cart));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(cartService.saveCart(cart)).thenReturn(Mono.empty());

        Mono<Order> result = orderService.createOrderFromCart();

        StepVerifier.create(result)
                .expectNextMatches(order -> order.getTotalSum().equals(new BigDecimal("20.00")) &&
                        order.getItems().size() == 1 &&
                        order.getItems().get(0).getQuantity() == 2 &&
                        cart.getItems().isEmpty())
                .verifyComplete();

        verify(cartService).saveCart(cart);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrderFromCart_emptyCart_throwsException() {
        cart.setItems(new ArrayList<>());
        when(cartService.getCurrentUserCart()).thenReturn(Mono.just(cart));

        Mono<Order> result = orderService.createOrderFromCart();

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof EmptyCartException &&
                        throwable.getMessage().equals("Cannot create order from empty cart"))
                .verify();

        verify(cartService).getCurrentUserCart();
        verifyNoInteractions(orderRepository);
    }

    @Test
    void getAllOrders_returnsOrders() {
        Long userId = 1L;
        Order order = new Order();
        order.setUserId(userId);

        when(orderRepository.findByUserId(userId)).thenReturn(Flux.just(order));

        Flux<Order> result = orderService.getAllOrders(userId);

        StepVerifier.create(result)
                .expectNext(order)
                .verifyComplete();

        verify(orderRepository).findByUserId(userId);
    }

    @Test
    void getOrderById_returnsOrder() {
        Order order = new Order();
        order.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Mono.just(order));

        Mono<Order> result = orderService.getOrderById(1L);

        StepVerifier.create(result)
                .expectNext(order)
                .verifyComplete();

        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderById_notFound_throwsException() {
        when(orderRepository.findById(1L)).thenReturn(Mono.empty());

        Mono<Order> result = orderService.getOrderById(1L);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof OrderNotFoundException &&
                        throwable.getMessage().equals("Order not found"))
                .verify();

        verify(orderRepository).findById(1L);
    }
}