package ru.yandex.intershop.configuration;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.yandex.intershop.client.PaymentClient;
import ru.yandex.intershop.service.AuthService;
import ru.yandex.intershop.service.CartService;
import ru.yandex.intershop.service.ItemService;
import ru.yandex.intershop.service.OrderService;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {
    @Bean
    ItemService itemService() {
        return mock(ItemService.class);
    }

    @Bean
    CartService cartService() {
        return mock(CartService.class);
    }

    @Bean
    OrderService orderService() {
        return mock(OrderService.class);
    }

    @Bean
    AuthService authService() {
        return Mockito.mock(AuthService.class);
    }

    @Bean
    PaymentClient paymentClient() {
        return mock(PaymentClient.class);
    }

}