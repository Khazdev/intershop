package ru.yandex.intershop.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.intershop.dto.ItemDto;
import ru.yandex.intershop.model.Cart;
import ru.yandex.intershop.model.CartItem;
import ru.yandex.intershop.model.Item;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

@UtilityClass
public class ItemToItemDtoMapper {

    public static ItemDto map(Item from, int count) {
        return ItemDto.builder()
                .id(from.getId())
                .count(count)
                .description(from.getDescription())
                .price(from.getPrice())
                .imgPath(from.getImgPath())
                .build();
    }

    public static List<ItemDto> mapList(List<Item> fromList, Cart currentUserCart) {
        Map<Long, Integer> itemCountMap = currentUserCart.getItems().stream()
                .collect(Collectors.toMap(
                        cartItem -> cartItem.getItem().getId(),
                        CartItem::getQuantity));

        return emptyIfNull(fromList).stream()
                .map(item -> map(item, itemCountMap.getOrDefault(item.getId(), 0)))
                .collect(Collectors.toList());
    }
}