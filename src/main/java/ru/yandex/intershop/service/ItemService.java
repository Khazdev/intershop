package ru.yandex.intershop.service;

import ru.yandex.intershop.model.Item;
import ru.yandex.intershop.repository.ItemDao;

import java.util.List;

public interface ItemService {

    List<Item> findItems();
}
