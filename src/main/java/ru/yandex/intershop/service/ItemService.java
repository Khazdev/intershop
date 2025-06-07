package ru.yandex.intershop.service;

import ru.yandex.intershop.model.Item;

import java.util.List;

public interface ItemService {

    List<Item> findItems();
    Item getItemById(Long id);
}
