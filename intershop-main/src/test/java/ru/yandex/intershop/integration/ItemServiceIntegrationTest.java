package ru.yandex.intershop.integration;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;
import ru.yandex.intershop.configuration.CacheConfig;
import ru.yandex.intershop.configuration.RedisTestConfig;
import ru.yandex.intershop.enums.SortType;
import ru.yandex.intershop.model.Item;
import ru.yandex.intershop.repository.ItemRepository;
import ru.yandex.intershop.service.ItemService;
import ru.yandex.intershop.service.ItemServiceImpl;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Import({ItemServiceImpl.class, RedisTestConfig.class, CacheConfig.class, JacksonAutoConfiguration.class})
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    @Qualifier("itemListReactiveRedisTemplate")
    private ReactiveRedisTemplate<String, List<Item>> itemListReactiveRedisTemplate;

    @Container
    private static final RedisContainer redisContainer = RedisTestConfig.REDIS_CONTAINER;

    private Item item;
    private Item item2;

    @DynamicPropertySource
    private static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379).toString());
    }

    @BeforeEach
    void setUp() {
        item = new Item();
        item.setPrice(new BigDecimal("100.00"));
        item.setTitle("Test Item");
        item.setDescription("Test Description");

        item = itemRepository.save(item).block();

        item2 = new Item();
        item2.setPrice(new BigDecimal("50.00"));
        item2.setTitle("Test Item 2");
        item2.setDescription("Test Description 2");

        item2 = itemRepository.save(item2).block();

    }

    @Test
    @DisplayName("Поиск товаров должен кешировать результаты")
    void findItems_shouldCacheResults() {

        // Первый вызов (должен закешироваться)
        StepVerifier.create(itemService.findItems("Item", SortType.NO, 1, 10))
                .assertNext(page -> {
                    assertThat(page.getContent()).hasSize(2);
                })
                .verifyComplete();

        // Удаляем из БД
        itemRepository.deleteAll().block();

        // Второй вызов (должен быть из кеша)
        StepVerifier.create(itemService.findItems("Item", SortType.NO, 1, 10))
                .assertNext(page -> {
                    assertThat(page.getContent()).hasSize(2);
                })
                .verifyComplete();

        // Проверяем кеш
        StepVerifier.create(itemListReactiveRedisTemplate.opsForValue().get("items:Item-NO-1-10"))
                .assertNext(list -> assertThat(list).hasSize(2))
                .verifyComplete();
    }
}