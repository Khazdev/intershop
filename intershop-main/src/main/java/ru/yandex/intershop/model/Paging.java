package ru.yandex.intershop.model;

public record Paging(int pageNumber, int pageSize, boolean hasNext, boolean hasPrevious) {
}