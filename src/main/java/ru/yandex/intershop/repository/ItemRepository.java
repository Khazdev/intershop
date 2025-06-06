package ru.yandex.intershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<ItemDao, Long> {
}