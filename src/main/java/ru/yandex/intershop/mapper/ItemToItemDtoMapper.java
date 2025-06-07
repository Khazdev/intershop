package ru.yandex.intershop.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.intershop.dto.ItemDto;
import ru.yandex.intershop.model.Item;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

@UtilityClass
public class ItemToItemDtoMapper {

    public static ItemDto map(Item fromDao) {
        return ItemDto.builder()
                .id(fromDao.getId())
                .count(fromDao.getCount())
                .description(fromDao.getDescription())
                .price(fromDao.getPrice())
                .imgPath(fromDao.getImgPath())
                .build();
    }

    public static List<ItemDto> mapList(List<Item> fromList) {
        return emptyIfNull(fromList).stream().map(ItemToItemDtoMapper::map).collect(Collectors.toList());
    }
}
