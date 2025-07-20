package ru.yandex.intershop.service;

import reactor.core.publisher.Mono;
import ru.yandex.intershop.model.User;

public interface AuthService {

    Mono<Long> getAuthenticatedUserId();

    Mono<User> getCurrentUserMono();
}
