package ru.yandex.intershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.intershop.model.User;
import ru.yandex.intershop.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;

    public Mono<Long> getAuthenticatedUserId() {
        return getCurrentUserMono()
                .map(User::getId)
                .switchIfEmpty(Mono.empty());
    }

    public Mono<User> getCurrentUserMono() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth != null && auth.isAuthenticated()
                        && !"anonymousUser".equals(auth.getPrincipal()))
                .map(Authentication::getName)
                .flatMap(userRepository::findByUsername)
                .switchIfEmpty(Mono.empty());
    }
}