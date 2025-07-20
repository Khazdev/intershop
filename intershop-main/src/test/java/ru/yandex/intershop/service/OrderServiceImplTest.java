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
import ru.yandex.intershop.client.PaymentClient;
import ru.yandex.intershop.client.payment.model.PaymentRequest;
import ru.yandex.intershop.client.payment.model.PaymentResponse;
import ru.yandex.intershop.exception.EmptyCartException;
import ru.yandex.intershop.exception.OrderNotFoundException;
import ru.yandex.intershop.model.*;
import ru.yandex.intershop.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @Mock
    private PaymentClient paymentClient;

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
            Item item = new Item();
            item.setId(1L);
            item.setPrice(new BigDecimal("10.00"));

            CartItem cartItem = new CartItem();
            cartItem.setItemId(1L);
            cartItem.setQuantity(2);
            cartItem.setItem(item);

            Cart cart = new Cart();
            cart.setUserId(123L);
            cart.setItems(new ArrayList<>(List.of(cartItem)));

            BigDecimal total = new BigDecimal("20.00");

            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setStatus(PaymentResponse.StatusEnum.SUCCESS);

            Order savedOrder = new Order();
            savedOrder.setUserId(123L);
            savedOrder.setTotalSum(total);
            OrderItem orderItem = new OrderItem();
            orderItem.setItemId(1L);
            orderItem.setQuantity(2);
            orderItem.setPrice(new BigDecimal("10.00"));
            savedOrder.setItems(new ArrayList<>(List.of(orderItem)));
            savedOrder.setUserOrderNumber(1L);

            when(cartService.getCurrentUserCart()).thenReturn(Mono.just(cart));
            when(cartService.calculateTotal(cart)).thenReturn(Mono.just(total));
            when(paymentClient.processPayment(any(PaymentRequest.class))).thenReturn(Mono.just(paymentResponse));
            when(orderRepository.findByUserId(123L)).thenReturn(Flux.empty());
            when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(savedOrder));
            when(cartService.saveCart(any(Cart.class))).thenReturn(Mono.empty());

            StepVerifier.create(orderService.createOrderFromCart())
                    .expectNextMatches(order ->
                            order.getUserId().equals(123L) &&
                                    order.getTotalSum().equals(total) &&
                                    order.getItems().size() == 1 &&
                                    order.getItems().get(0).getItemId().equals(1L) &&
                                    order.getItems().get(0).getQuantity() == 2 &&
                                    order.getItems().get(0).getPrice().equals(new BigDecimal("10.00")) &&
                                    order.getUserOrderNumber().equals(1L))
                    .verifyComplete();

            verify(cartService).saveCart(argThat(c -> c.getItems().isEmpty()));
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