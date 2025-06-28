package ru.yandex.intershop.service;

import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;
import ru.yandex.intershop.enums.SortType;
import ru.yandex.intershop.model.Item;


public interface ItemService {

    Mono<Page<Item>> findItems(String search, SortType sort, int pageNumber, int pageSize);
    Item getItemById(Long id);
}
