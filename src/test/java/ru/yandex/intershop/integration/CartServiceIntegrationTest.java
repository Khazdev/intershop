//package ru.yandex.intershop.integration;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.FilterType;
//import ru.yandex.intershop.enums.ActionType;
//import ru.yandex.intershop.model.Cart;
//import ru.yandex.intershop.model.Item;
//import ru.yandex.intershop.repository.ItemRepository;
//import ru.yandex.intershop.service.CartService;
//import ru.yandex.intershop.service.CartServiceImpl;
//
//import java.math.BigDecimal;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//@DataJpaTest
//@ComponentScan(basePackages = "ru.yandex.intershop.service",
//        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CartServiceImpl.class))
//class CartServiceIntegrationTest {
//
//    @Autowired
//    private CartService cartService;
//
//    @Autowired
//    private ItemRepository itemRepository;
//
//    @Test
//    void updateCartItem_andCalculateTotal_success() {
//        Item item = new Item();
//        item.setPrice(new BigDecimal("10.00"));
//        itemRepository.save(item);
//
//        cartService.updateCartItem(item.getId(), ActionType.PLUS);
//        Cart cart = cartService.getCurrentUserCart();
//
//        assertEquals(1, cart.getItems().size());
//        assertEquals(1, cart.getItems().get(0).getQuantity());
//        assertEquals(new BigDecimal("10.00"), cartService.calculateTotal(cart));
//
//        cartService.updateCartItem(item.getId(), ActionType.PLUS);
//        assertEquals(2, cart.getItems().get(0).getQuantity());
//        assertEquals(new BigDecimal("20.00"), cartService.calculateTotal(cart));
//
//        cartService.updateCartItem(item.getId(), ActionType.MINUS);
//        assertEquals(1, cart.getItems().get(0).getQuantity());
//        assertEquals(new BigDecimal("10.00"), cartService.calculateTotal(cart));
//
//        cartService.updateCartItem(item.getId(), ActionType.MINUS);
//        assertTrue(cart.getItems().isEmpty());
//        assertEquals(BigDecimal.ZERO, cartService.calculateTotal(cart));
//    }
//}