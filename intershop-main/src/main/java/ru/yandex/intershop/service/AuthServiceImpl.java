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
        return checkAuthentication()
                .filter(isAuthenticated -> isAuthenticated)
                .map(isAuthenticated -> ReactiveSecurityContextHolder.getContext())
                .flatMap(securityContext -> securityContext
                        .map(SecurityContext::getAuthentication)
                        .map(Authentication::getName)
                        .flatMap(userRepository::findByUsername))
                .switchIfEmpty(Mono.empty());
    }

    public Mono<Boolean> isAuthenticated() {
        return checkAuthentication()
                .defaultIfEmpty(false);
    }

    private Mono<Boolean> checkAuthentication() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(auth -> auth != null && auth.isAuthenticated()
                        && !"anonymousUser".equals(auth.getPrincipal()));
    }
}