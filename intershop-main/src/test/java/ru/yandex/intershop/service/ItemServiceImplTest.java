package ru.yandex.intershop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.intershop.enums.SortType;
import ru.yandex.intershop.model.Item;
import ru.yandex.intershop.repository.ItemRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Mock
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @Mock
    private ReactiveValueOperations<String, Object> reactiveValueOperations;

    @BeforeEach
    void setUp() {
        when(reactiveRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        when(reactiveValueOperations.get(anyString())).thenReturn(Mono.empty());
    }

    @Test
    void findItems_noSearch_returnsPagedItems() {
        when(reactiveValueOperations.set(anyString(), any(), any())).thenReturn(Mono.just(true));

        Item item = new Item();
        PageRequest pageable = PageRequest.of(0, 10, Sort.unsorted());
        when(itemRepository.findAll(pageable.getSort())).thenReturn(Flux.just(item));
        when(itemRepository.count()).thenReturn(Mono.just(1L));

        Mono<Page<Item>> result = itemService.findItems("", SortType.NO, 1, 10);

        StepVerifier.create(result)
                .expectNextMatches(page -> page.getContent().size() == 1 &&
                        page.getTotalElements() == 1 &&
                        page.getNumber() == 0 &&
                        page.getSize() == 10)
                .verifyComplete();

        verify(itemRepository).findAll(pageable.getSort());
        verify(itemRepository).count();
    }

    @Test
    void findItems_withSearch_returnsFilteredItems() {
        when(reactiveValueOperations.set(anyString(), any(), any())).thenReturn(Mono.just(true));

        Item item = new Item();
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("title").ascending());
        when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("test", "test", pageable))
                .thenReturn(Flux.just(item));
        when(itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("test", "test"))
                .thenReturn(Mono.just(1L));

        Mono<Page<Item>> result = itemService.findItems("test", SortType.ALPHA, 1, 10);

        StepVerifier.create(result)
                .expectNextMatches(page -> page.getContent().size() == 1 &&
                        page.getTotalElements() == 1 &&
                        page.getNumber() == 0 &&
                        page.getSize() == 10)
                .verifyComplete();

        verify(itemRepository).findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("test", "test", pageable);
        verify(itemRepository).countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("test", "test");
    }

    @Test
    void getItemById_returnsItem() {
        when(reactiveValueOperations.set(anyString(), any(), any())).thenReturn(Mono.just(true));

        Item item = new Item();
        item.setId(1L);
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));

        Mono<Item> result = itemService.getItemById(1L);

        StepVerifier.create(result)
                .expectNext(item)
                .verifyComplete();

        verify(itemRepository).findById(1L);
    }

    @Test
    void getItemById_notFound_returnsEmpty() {
        when(itemRepository.findById(1L)).thenReturn(Mono.empty());

        Mono<Item> result = itemService.getItemById(1L);

        StepVerifier.create(result)
                .verifyComplete();

        verify(itemRepository).findById(1L);
    }
}