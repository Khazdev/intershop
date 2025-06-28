package ru.yandex.intershop.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.ArrayList;
import java.util.List;

@Table("cart")
@Getter
@Setter
public class Cart {
    @Id
    private Long id;

    @Transient
    @MappedCollection(idColumn = "cart_id")
    private List<CartItem> items = new ArrayList<>();

    private Long userId;

}