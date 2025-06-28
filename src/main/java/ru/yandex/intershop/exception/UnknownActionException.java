package ru.yandex.intershop.exception;

public class UnknownActionException extends RuntimeException {
    public UnknownActionException(String message) {
        super(message);
    }
}
