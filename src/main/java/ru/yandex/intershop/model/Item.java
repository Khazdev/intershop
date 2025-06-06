package ru.yandex.intershop.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Item {

    private Long id;

    private String title;

    private String description;

    private String imgPath;

    private BigDecimal price;

    private int count;

}