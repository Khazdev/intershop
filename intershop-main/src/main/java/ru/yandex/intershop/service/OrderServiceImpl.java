package ru.yandex.intershop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.intershop.client.PaymentClient;
import ru.yandex.intershop.client.payment.model.PaymentRequest;
import ru.yandex.intershop.client.payment.model.PaymentResponse;
import ru.yandex.intershop.exception.EmptyCartException;
import ru.yandex.intershop.exception.OrderNotFoundException;
import ru.yandex.intershop.exception.PaymentFailedException;
import ru.yandex.intershop.model.Cart;
import ru.yandex.intershop.model.CartItem;
import ru.yandex.intershop.model.Order;
import ru.yandex.intershop.model.OrderItem;
import ru.yandex.intershop.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final PaymentClient paymentClient;

    @Override
    public Mono<Order> createOrderFromCart() {
        return cartService.getCurrentUserCart()
                .doOnNext(cart -> log.info("Текущая корзина перед очисткой: {}", cart.getItems()))
                .flatMap(cart -> {
                    if (cart.getItems().isEmpty()) {
                        return Mono.error(new EmptyCartException("Cannot create order from empty cart"));
                    }
                    return cartService.calculateTotal(cart)
                            .flatMap(total -> {
                                PaymentRequest paymentRequest = new PaymentRequest()
                                        .userId(cart.getUserId())
                                        .amount(total);

                                return paymentClient.processPayment(paymentRequest)
                                        .flatMap(paymentResponse -> {
                                            if (PaymentResponse.StatusEnum.SUCCESS.equals(paymentResponse.getStatus())) {
                                                return createOrder(cart, total);
                                            } else {
                                                return Mono.error(new PaymentFailedException(paymentResponse.getMessage()));
                                            }
                                        });
                            });
                });
    }

    @Override
    public Flux<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Mono<Order> getOrderById(Long id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new OrderNotFoundException("Order not found")));
    }

    private Mono<Order> createOrder(Cart cart, BigDecimal total) {
        Order order = new Order();
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(cartItem.getItem());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getItem().getPrice());
            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        order.setTotalSum(total);

        return orderRepository.save(order)
                .flatMap(savedOrder -> {
                    log.info("Корзина перед очисткой: {}", cart.getItems());
                    cart.clearItems();
                    log.info("Корзина после очистки: {}", cart.getItems());
                    return cartService.saveCart(cart)
                            .thenReturn(savedOrder);
                });
    }
}

