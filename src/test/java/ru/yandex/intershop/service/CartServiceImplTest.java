//package ru.yandex.intershop.service;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import ru.yandex.intershop.enums.ActionType;
//import ru.yandex.intershop.model.Cart;
//import ru.yandex.intershop.model.CartItem;
//import ru.yandex.intershop.model.Item;
//import ru.yandex.intershop.repository.CartItemRepository;
//import ru.yandex.intershop.repository.CartRepository;
//import ru.yandex.intershop.repository.ItemRepository;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class CartServiceImplTest {
//
//    @Mock
//    private CartRepository cartRepository;
//
//    @Mock
//    private ItemRepository itemRepository;
//
//    @Mock
//    private CartItemRepository cartItemRepository;
//
//    @InjectMocks
//    private CartServiceImpl cartService;
//
//    private Cart cart;
//    private Item item;
//    private CartItem cartItem;
//
//    @BeforeEach
//    void setUp() {
//        cart = new Cart();
//        cart.setUserId(1L);
//        cart.setItems(new ArrayList<>());
//
//        item = new Item();
//        item.setId(1L);
//        item.setPrice(new BigDecimal("10.00"));
//
//        cartItem = new CartItem();
//        cartItem.setCart(cart);
//        cartItem.setItem(item);
//        cartItem.setQuantity(1);
//    }
//
//    @Test
//    void getCurrentUserCart_existingCart_returnsCart() {
//        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
//
//        Cart result = cartService.getCurrentUserCart();
//
//        assertNotNull(result);
//        assertEquals(1L, result.getUserId());
//        verify(cartRepository).findByUserId(1L);
//    }
//
//    @Test
//    void getCurrentUserCart_noCart_createsNewCart() {
//        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
//        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
//
//        Cart result = cartService.getCurrentUserCart();
//
//        assertNotNull(result);
//        assertEquals(1L, result.getUserId());
//        verify(cartRepository).save(any(Cart.class));
//    }
//
//    @Test
//    void updateCartItem_plusNewItem_addsToCart() {
//        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
//        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
//        when(cartItemRepository.findByCartAndItem(cart, item)).thenReturn(Optional.empty());
//        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
//
//        cartService.updateCartItem(1L, ActionType.PLUS);
//
//        assertEquals(1, cart.getItems().size());
//        verify(cartItemRepository).save(any(CartItem.class));
//    }
//
//    @Test
//    void updateCartItem_plusExistingItem_incrementsQuantity() {
//        cart.getItems().add(cartItem);
//        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
//        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
//        when(cartItemRepository.findByCartAndItem(cart, item)).thenReturn(Optional.of(cartItem));
//        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
//
//        cartService.updateCartItem(1L, ActionType.PLUS);
//
//        assertEquals(2, cartItem.getQuantity());
//        verify(cartItemRepository).save(cartItem);
//    }
//
//    @Test
//    void updateCartItem_minusSingleItem_removesItem() {
//        cart.getItems().add(cartItem);
//        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
//        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
//        when(cartItemRepository.findByCartAndItem(cart, item)).thenReturn(Optional.of(cartItem));
//
//        cartService.updateCartItem(1L, ActionType.MINUS);
//
//        assertTrue(cart.getItems().isEmpty());
//        verify(cartItemRepository).delete(cartItem);
//    }
//
//    @Test
//    void calculateTotal_withItems_returnsCorrectTotal() {
//        cart.getItems().add(cartItem);
//        BigDecimal result = cartService.calculateTotal(cart);
//
//        assertEquals(new BigDecimal("10.00"), result);
//    }
//}