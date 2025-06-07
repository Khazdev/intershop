package ru.yandex.intershop.service;

import ru.yandex.intershop.model.Order;

import java.util.List;

public interface OrderService {
    Order createOrderFromCart();
    List<Order> getAllOrders();
    Order getOrderById(Long id);
}
