package ru.yandex.intershop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.intershop.exception.EmptyCartException;
import ru.yandex.intershop.exception.OrderNotFoundException;
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

    @Override
    public Mono<Order> createOrderFromCart() {
        return cartService.getCurrentUserCart()
                .doOnNext(cart -> log.info("Текущая корзина перед очисткой: {}", cart.getItems()))
                .flatMap(cart -> {
                    if (cart.getItems().isEmpty()) {
                        return Mono.error(new EmptyCartException("Cannot create order from empty cart"));
                    }

                    Order order = new Order();
                    List<OrderItem> orderItems = new ArrayList<>();
                    BigDecimal total = BigDecimal.ZERO;

                    for (CartItem cartItem : cart.getItems()) {
                        OrderItem orderItem = new OrderItem();
                        orderItem.setItem(cartItem.getItem());
                        orderItem.setQuantity(cartItem.getQuantity());
                        orderItem.setPrice(cartItem.getItem().getPrice());
                        orderItems.add(orderItem);
                        total = total.add(cartItem.getItem().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
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
}

