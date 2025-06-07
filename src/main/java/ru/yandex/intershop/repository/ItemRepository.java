package ru.yandex.intershop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.intershop.model.Item;

public interface ItemRepository extends JpaRepository<ItemDao, Long> {
public interface ItemRepository extends JpaRepository<Item, Long> {
}