package ru.yandex.intershop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.intershop.model.Cart;
import ru.yandex.intershop.model.CartItem;
import ru.yandex.intershop.model.Item;
import ru.yandex.intershop.model.Order;
import ru.yandex.intershop.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.setUserId(1L);
        cart.setItems(new ArrayList<>());

        Item item = new Item();
        item.setId(1L);
        item.setPrice(new BigDecimal("10.00"));

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setItem(item);
        cartItem.setQuantity(2);
        cart.getItems().add(cartItem);
    }

    @Test
    void createOrderFromCart_validCart_createsOrder() {
        when(cartService.getCurrentUserCart()).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.createOrderFromCart();

        assertNotNull(result);
        assertEquals(new BigDecimal("20.00"), result.getTotalSum());
        assertEquals(1, result.getItems().size());
        assertTrue(cart.getItems().isEmpty());
        verify(cartService).saveCart(cart);
    }

    @Test
    void createOrderFromCart_emptyCart_throwsException() {
        cart.setItems(new ArrayList<>());
        when(cartService.getCurrentUserCart()).thenReturn(cart);

        assertThrows(IllegalStateException.class, () -> orderService.createOrderFromCart());
    }

    @Test
    void getAllOrders_returnsOrders() {
        List<Order> orders = new ArrayList<>();
        when(orderRepository.findAll()).thenReturn(orders);

        List<Order> result = orderService.getAllOrders();

        assertEquals(orders, result);
        verify(orderRepository).findAll();
    }
}