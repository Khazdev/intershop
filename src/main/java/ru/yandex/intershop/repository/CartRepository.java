package ru.yandex.intershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.intershop.model.Cart;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(Long userId);
}