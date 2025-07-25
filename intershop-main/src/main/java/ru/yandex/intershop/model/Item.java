package ru.yandex.intershop.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("items")
@Getter
@Setter
public class Item {
    @Id
    private Long id;
    private String title;
    private String description;
    @Column("img_path")
    private String imgPath;
    private BigDecimal price;
    @Transient
    private int count;
    @Column("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}