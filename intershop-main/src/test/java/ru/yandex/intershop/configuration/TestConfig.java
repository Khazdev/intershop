package ru.yandex.intershop.configuration;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.yandex.intershop.service.CartService;
import ru.yandex.intershop.service.ItemService;
import ru.yandex.intershop.service.OrderService;

@TestConfiguration
public class TestConfig {
    @Bean
    ItemService itemService() {
        return Mockito.mock(ItemService.class);
    }

    @Bean
    CartService cartService() {
        return Mockito.mock(CartService.class);
    }

    @Bean
    OrderService orderService() {
        return Mockito.mock(OrderService.class);
    }

}