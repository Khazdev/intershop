package ru.yandex.intershop.repository;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@Table(name = "items")
public class ItemDao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    @Column(name = "img_path")
    private String imgPath;
    private BigDecimal price;
    @Transient
    private int count;
    @Column(name = "created_at", updatable = false, insertable = false)
    private Timestamp createdAt;
}