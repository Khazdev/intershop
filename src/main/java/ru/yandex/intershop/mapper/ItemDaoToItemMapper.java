package ru.yandex.intershop.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.intershop.model.Item;
import ru.yandex.intershop.repository.ItemDao;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

@UtilityClass
public class ItemDaoToItemMapper {

    public static Item map(ItemDao fromDao) {
        return Item.builder()
                .id(fromDao.getId())
                .count(fromDao.getCount())
                .description(fromDao.getDescription())
                .price(fromDao.getPrice())
                .imgPath(fromDao.getImgPath())
                .build();
    }

    public static List<Item> mapList(List<ItemDao> fromList) {
        return emptyIfNull(fromList).stream().map(ItemDaoToItemMapper::map).collect(Collectors.toList());
    }
}
