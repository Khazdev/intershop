package ru.yandex.intershop.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.intershop.client.PaymentClient;
import ru.yandex.intershop.client.payment.model.PaymentRequest;
import ru.yandex.intershop.client.payment.model.PaymentResponse;
import ru.yandex.intershop.configuration.TestConfig;
import ru.yandex.intershop.dto.UpdateCartForm;
import ru.yandex.intershop.enums.ActionType;
import ru.yandex.intershop.enums.SortType;
import ru.yandex.intershop.model.Cart;
import ru.yandex.intershop.model.Item;
import ru.yandex.intershop.model.Order;
import ru.yandex.intershop.service.CartService;
import ru.yandex.intershop.service.ItemService;
import ru.yandex.intershop.service.OrderService;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@WebFluxTest(MainController.class)
@Import(TestConfig.class)
class MainControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ItemService itemService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentClient paymentClient;

    @BeforeEach
    void setUp() {
        reset(itemService, cartService, orderService);
    }

    @Test
    void root_redirectsToMainItems() {
        webTestClient.get().uri("/")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/main/items");
    }

    @Test
    void showItems_withDefaultParams_returnsMainView() {
        Item item = new Item();
        Page<Item> page = new PageImpl<>(List.of(item));
        Cart cart = new Cart();
        when(itemService.findItems("", SortType.NO, 1, 10)).thenReturn(Mono.just(page));
        when(cartService.getCurrentUserCart()).thenReturn(Mono.just(cart));

        webTestClient.get().uri("/main/items")
                .exchange()
                .expectStatus().isOk();

        verify(itemService).findItems("", SortType.NO, 1, 10);
        verify(cartService).getCurrentUserCart();
    }

    @Test
    void showItems_withSearchAndSort_returnsMainViewWithCorrectAttributes() {
        Item item = new Item();
        Page<Item> page = new PageImpl<>(List.of(item));
        Cart cart = new Cart();
        when(itemService.findItems("test", SortType.ALPHA, 2, 5)).thenReturn(Mono.just(page));
        when(cartService.getCurrentUserCart()).thenReturn(Mono.just(cart));

        webTestClient.get().uri(uriBuilder ->
                        uriBuilder.path("/main/items")
                                .queryParam("search", "test")
                                .queryParam("sort", "ALPHA")
                                .queryParam("pageNumber", "2")
                                .queryParam("pageSize", "5")
                                .build())
                .exchange()
                .expectStatus().isOk();

        verify(itemService).findItems("test", SortType.ALPHA, 2, 5);
        verify(cartService).getCurrentUserCart();
    }

    @Test
    void updateMainCart_updatesCartAndRedirects() {
        UpdateCartForm form = new UpdateCartForm();
        form.setAction(ActionType.PLUS);
        when(cartService.updateCartItem(1L, ActionType.PLUS)).thenReturn(Mono.empty());

        webTestClient.post().uri("/main/items/1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("action=PLUS")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/main/items");

        verify(cartService).updateCartItem(1L, ActionType.PLUS);
    }

    @Test
    void buy_createsOrderAndRedirects() {
        Order order = new Order();
        order.setId(1L);
        when(orderService.createOrderFromCart()).thenReturn(Mono.just(order));

        PaymentResponse successResponse = new PaymentResponse()
                .status(PaymentResponse.StatusEnum.SUCCESS)
                .transactionId(UUID.randomUUID());

        when(paymentClient.processPayment(any(PaymentRequest.class)))
                .thenReturn(Mono.just(successResponse));
        webTestClient.post().uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/orders/1?newOrder=true");

        verify(orderService).createOrderFromCart();
    }
}