package ru.yandex.intershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.intershop.mapper.ItemDaoToItemMapper;
import ru.yandex.intershop.model.Item;
import ru.yandex.intershop.repository.ItemDao;
import ru.yandex.intershop.repository.ItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Override
    public List<Item> findItems() {
        return ItemDaoToItemMapper.mapList(itemRepository.findAll());
    }
}