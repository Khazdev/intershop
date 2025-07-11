package ru.yandex.intershop.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("cart_item")
@Getter
@Setter
@ToString
public class CartItem {
    @Id
    private Long id;

    @Column("cart_id")
    private Long cartId;

    @Column("item_id")
    private Long itemId;

    private int quantity;

    @Transient
    private Cart cart;
    @Transient
    private Item item;
}