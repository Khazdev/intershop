package ru.yandex.intershop.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table("purchase_order")
@Getter
@Setter
public class Order {
    @Id
    private Long id;

    @Transient
    @MappedCollection(idColumn = "order_id")
    private List<OrderItem> items = new ArrayList<>();

    @Column("total_sum")
    private BigDecimal totalSum;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("user_id")
    private Long userId;

    @Column("user_order_number")
    private Long userOrderNumber;

}