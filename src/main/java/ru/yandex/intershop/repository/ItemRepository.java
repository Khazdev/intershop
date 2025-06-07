package ru.yandex.intershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.intershop.model.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Item getItemById(Long id);
}