package ru.yandex.intershop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.intershop.enums.SortType;
import ru.yandex.intershop.model.Item;
import ru.yandex.intershop.repository.ItemRepository;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private static final String CACHE_PREFIX = "items:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(2);
    private final ItemRepository itemRepository;
    private final ReactiveRedisTemplate<String, List<Item>> reactiveRedisTemplate;
    private final ReactiveRedisTemplate<String, Item> itemRedisTemplate;

    public Mono<Page<Item>> findItems(String search, SortType sort, int pageNumber, int pageSize) {
        String cacheKey = CACHE_PREFIX + search + "-" + sort + "-" + pageNumber + "-" + pageSize;
        PageRequest pageable = PageRequest.of(pageNumber - 1, pageSize, getSort(sort));

        return reactiveRedisTemplate.opsForValue().get(cacheKey)
                .doOnNext(items -> log.debug("Найдены данные в кеше. Количество элементов: {}", items.size()))
                .flatMap(cachedItems -> {
                    log.info("Возвращаем данные из кеша");
                    return Mono.<Page<Item>>just(new PageImpl<>(cachedItems, pageable, cachedItems.size()));
                })
                .onErrorResume(e -> {
                    log.info("Redis не доступен Запрашиваем из БД...");
                    return fetchFromDatabase(search, pageable);
                })
                .switchIfEmpty(
                        Mono.defer(() -> {
                            log.info("Данные не найдены в кеше. Запрашиваем из БД...");
                            return fetchFromDatabase(search, pageable).flatMap(page -> reactiveRedisTemplate.opsForValue()
                                            .set(cacheKey, page.getContent(), CACHE_TTL)
                                            .thenReturn(page))
                                    .onErrorResume(e -> {
                                        log.warn("Не удалось обновить кеш в Redis: {}", e.getMessage());
                                        return Mono.empty();
                                    });
                        })
                );
    }

    private Mono<Page<Item>> fetchFromDatabase(String search, PageRequest pageable) {
        return (search.isEmpty() ? fetchAllItems(pageable) : fetchFilteredItems(search, pageable));
    }

    private Mono<Page<Item>> fetchAllItems(PageRequest pageable) {
        return itemRepository.findAll(pageable.getSort())
                .skip(pageable.getOffset())
                .take(pageable.getPageSize())
                .collectList()
                .zipWith(itemRepository.count())
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    private Mono<Page<Item>> fetchFilteredItems(String search, PageRequest pageable) {
        return itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search, pageable)
                .collectList()
                .zipWith(itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search))
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    @Override
    public Mono<Item> getItemById(Long id) {
        String cacheKey = CACHE_PREFIX + "item-" + id;

        return itemRedisTemplate.opsForValue().get(cacheKey)
                .doOnNext(item -> log.debug("Найдены данные по одному элементу в кеше с id={}.", item.getId()))
                .onErrorResume(e -> {
                    log.info("Redis не доступен Запрашиваем из БД...");
                    return itemRepository.findById(id);
                })
                .switchIfEmpty(Mono.defer(() -> {
                            log.info("Данные по элементу с id={} не найдены в кеше. Запрашиваем из БД...", id);
                            return itemRepository.findById(id)
                                    .flatMap(item -> {
                                        if (item == null) {
                                            return Mono.empty();
                                        }
                                        return itemRedisTemplate.opsForValue()
                                                .set(cacheKey, item, CACHE_TTL)
                                                .thenReturn(item);
                                    });
                        }
                ));
    }

    private Sort getSort(SortType sort) {
        return switch (sort) {
            case ALPHA -> Sort.by("title").ascending();
            case PRICE -> Sort.by("price").ascending();
            case NO -> Sort.unsorted();
        };
    }
}