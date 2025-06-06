package ru.yandex.intershop.model;

import java.util.List;

public record Page<T>(
        List<T> items,
        String search,
        Paging paging
) {}