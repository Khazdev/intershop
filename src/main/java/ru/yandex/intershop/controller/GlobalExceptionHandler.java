package ru.yandex.intershop.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;
import ru.yandex.intershop.exception.EmptyCartException;
import ru.yandex.intershop.exception.ItemNotFoundException;
import ru.yandex.intershop.exception.OrderNotFoundException;
import ru.yandex.intershop.exception.UnknownActionException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ItemNotFoundException.class)
    public Mono<String> handleItemNotFoundException(ItemNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", "Товар не найден: " + ex.getMessage());
        return Mono.just("error");
    }

    @ExceptionHandler(UnknownActionException.class)
    public Mono<String> handleUnknownActionException(UnknownActionException ex, Model model) {
        model.addAttribute("errorMessage", "Неизвестное действие: " + ex.getMessage());
        return Mono.just("error");
    }

    @ExceptionHandler(EmptyCartException.class)
    public Mono<String> handleEmptyCartException(EmptyCartException ex, Model model) {
        model.addAttribute("errorMessage", "Невозможно создать заказ: корзина пуста");
        return Mono.just("error");
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public Mono<String> handleOrderNotFoundException(OrderNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", "Заказ не найден: " + ex.getMessage());
        return Mono.just("error");
    }

    @ExceptionHandler(Exception.class)
    public Mono<String> handleGeneralException(Exception ex, Model model) {
        model.addAttribute("errorMessage", "Произошла ошибка: " + ex.getMessage());
        return Mono.just("error");
    }
}