package ru.yandex.intershop.mapper;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.intershop.dto.ItemDto;
import ru.yandex.intershop.model.Cart;
import ru.yandex.intershop.model.CartItem;
import ru.yandex.intershop.model.Item;

import java.util.Collections;
import java.util.List;

@Slf4j
@UtilityClass
public class ItemToItemDtoMapper {

    public static ItemDto map(Item from, int count) {
        return ItemDto.builder()
                .id(from.getId())
                .count(count)
                .title(from.getTitle())
                .description(from.getDescription())
                .price(from.getPrice())
                .imgPath(from.getImgPath())
                .build();
    }

    public static Mono<List<ItemDto>> mapList(Flux<Item> items, Mono<Cart> cart) {
        return cart
                .flatMap(currentUserCart -> {
                    List<CartItem> cartItems = currentUserCart.getItems() != null
                            ? currentUserCart.getItems()
                            : Collections.emptyList();
                    return Flux.fromIterable(cartItems)
                            .collectMap(
                                    cartItem -> cartItem.getItem().getId(),
                                    CartItem::getQuantity
                            )
                            .flatMap(itemCountMap ->
                                    items.switchIfEmpty(Flux.fromIterable(Collections.emptyList()))
                                            .map(item -> map(item, itemCountMap.getOrDefault(item.getId(), 0)))
                                            .collectList()
                            );
                })
                .switchIfEmpty(items
                        .switchIfEmpty(Flux.fromIterable(Collections.emptyList()))
                        .map(item -> map(item, 0))
                        .collectList()
                .doOnError(error -> log.error("Error in mapList", error))
                .onErrorResume(error -> {
                    return items
                            .switchIfEmpty(Flux.fromIterable(Collections.emptyList()))
                            .map(item -> map(item, 0))
                            .collectList();
                }));
    }
}