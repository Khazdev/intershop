package ru.yandex.intershop.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.intershop.enums.ActionType;
import ru.yandex.intershop.model.Cart;
import ru.yandex.intershop.model.CartItem;
import ru.yandex.intershop.model.Item;
import ru.yandex.intershop.repository.CartRepository;
import ru.yandex.intershop.repository.ItemRepository;
import ru.yandex.intershop.repository.OrderRepository;
import ru.yandex.intershop.service.CartService;
import ru.yandex.intershop.service.CartServiceImpl;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataR2dbcTest
@ActiveProfiles("test")
@Import({CartServiceImpl.class})
class CartServiceIntegrationTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Item item;
    private Item item2;

    @BeforeEach
    void setUp() {
        item = new Item();
        item.setPrice(new BigDecimal("100.00"));
        item.setTitle("Test Item");
        item.setDescription("Test Description");

        item2 = new Item();
        item2.setPrice(new BigDecimal("50.00"));
        item2.setTitle("Test Item 2");
        item2.setDescription("Test Description 2");

        itemRepository.saveAll(List.of(item, item2))
                .collectList()
                .block();

        Cart cart = new Cart();
        cart.setUserId(1L);
        cartRepository.save(cart).block();
    }

    @Test
    void updateCartItem_andCalculateTotal_success() {
        Long itemId1 = item.getId();
        Mono<Void> updateMono1 = cartService.updateCartItem(itemId1, ActionType.PLUS);

        StepVerifier.create(updateMono1)
                .verifyComplete();

        Long itemId2 = item2.getId();
        Mono<Void> updateMono2 = cartService.updateCartItem(itemId2, ActionType.PLUS);

        StepVerifier.create(updateMono2)
                .verifyComplete();

        Mono<Cart> cartMono = cartService.getCurrentUserCart();

        StepVerifier.create(cartMono)
                .assertNext(cart -> {
                    assertEquals(2, cart.getItems().size(), "В корзине должно быть два товара");

                    CartItem cartItem1 = cart.getItems().stream()
                            .filter(item -> item.getItemId().equals(itemId1))
                            .findFirst()
                            .orElseThrow(() -> new AssertionError("Первый товар не попал в корзину"));
                    assertEquals(itemId1, cartItem1.getItemId(), "Id первого товара должен совпадать");
                    assertEquals(1, cartItem1.getQuantity(), "Первого товара должно быть 1 штука в корзине");
                    assertEquals(item.getPrice(), cartItem1.getItem().getPrice(), "Цена первого товара должна совпадать");

                    CartItem cartItem2 = cart.getItems().stream()
                            .filter(item -> item.getItemId().equals(itemId2))
                            .findFirst()
                            .orElseThrow(() -> new AssertionError("Второй товар не попал в корзину"));
                    assertEquals(itemId2, cartItem2.getItemId(), "Id второго товара должен совпадать");
                    assertEquals(1, cartItem2.getQuantity(), "Второго товара должно быть 1 штука в корзине");
                    assertEquals(item2.getPrice(), cartItem2.getItem().getPrice(), "Цена второго товара должна совпадать");
                })
                .verifyComplete();

        Mono<BigDecimal> totalMono = cartMono.flatMap(cartService::calculateTotal);

        StepVerifier.create(totalMono)
                .expectNext(new BigDecimal("150.00"))
                .verifyComplete();
    }
}