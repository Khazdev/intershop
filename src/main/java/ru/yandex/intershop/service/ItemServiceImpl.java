package ru.yandex.intershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.intershop.enums.SortType;
import ru.yandex.intershop.model.Item;
import ru.yandex.intershop.repository.ItemRepository;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Override
    public Mono<Page<Item>> findItems(String search, SortType sort, int pageNumber, int pageSize) {
        PageRequest pageable = PageRequest.of(pageNumber - 1, pageSize, getSort(sort));
        return search.isEmpty()
                ? fetchAllItems(pageable)
                : fetchFilteredItems(search, pageable);
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
    public Item getItemById(Long id) {
        return new Item();
//        return itemRepository.getItemById(id);
    }

    private Sort getSort(SortType sort) {
        return switch (sort) {
            case ALPHA -> Sort.by("title").ascending();
            case PRICE -> Sort.by("price").ascending();
            case NO -> Sort.unsorted();
        };
    }
}