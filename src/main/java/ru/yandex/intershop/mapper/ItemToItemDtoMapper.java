package ru.yandex.intershop.mapper;

import lombok.experimental.UtilityClass;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.intershop.dto.ItemDto;
import ru.yandex.intershop.model.Cart;
import ru.yandex.intershop.model.CartItem;
import ru.yandex.intershop.model.Item;

import java.util.Collections;
import java.util.List;

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
        return cart.flatMap(currentUserCart ->
                Flux.fromIterable(currentUserCart.getItems())
                        .collectMap(
                                cartItem -> cartItem.getItem().getId(),
                                CartItem::getQuantity
                        )
                        .flatMap(itemCountMap ->
                                items.switchIfEmpty(Flux.fromIterable(Collections.emptyList()))
                                        .map(item -> map(item, itemCountMap.getOrDefault(item.getId(), 0)))
                                        .collectList()
                        )
        );
    }
}