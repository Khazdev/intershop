package ru.yandex.intershop.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.intershop.model.Order;
import ru.yandex.intershop.repository.OrderRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;

    @Transactional
    public Order createOrderFromCart() {
//        Cart cart = cartService.getCurrentUserCart();
//        if (cart.getItems().isEmpty()) {
//            throw new IllegalStateException("Cannot create order from empty cart");
//        }
//
//        Order order = new Order();
//        List<OrderItem> orderItems = new ArrayList<>();
//        BigDecimal total = BigDecimal.ZERO;
//
//        for (CartItem cartItem : cart.getItems()) {
//            OrderItem orderItem = new OrderItem();
//            orderItem.setItem(cartItem.getItem());
//            orderItem.setQuantity(cartItem.getQuantity());
//            orderItem.setPrice(cartItem.getItem().getPrice());
//            orderItems.add(orderItem);
//
//            total = total.add(cartItem.getItem().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
//        }
//
//        order.setItems(orderItems);
//        order.setTotalSum(total);
//
//        Order savedOrder = orderRepository.save(order);
//
//        cart.getItems().clear();
//        cartService.saveCart(cart);
//
//        return savedOrder;
        return new Order();
    }

    public List<Order> getAllOrders() {
//        return orderRepository.findAll();
        return List.of(new Order());
    }
//
    public Order getOrderById(Long id) {
//        return orderRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        return new Order();
    }
}

