package ru.yandex.intershop.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ItemDto {

    private Long id;

    private String title;

    private String description;

    private String imgPath;

    private BigDecimal price;

    private int count;

}