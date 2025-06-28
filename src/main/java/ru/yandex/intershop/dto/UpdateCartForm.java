package ru.yandex.intershop.dto;

import lombok.Getter;
import lombok.Setter;
import ru.yandex.intershop.enums.ActionType;

@Getter
@Setter
public class UpdateCartForm {
    private ActionType action;
}