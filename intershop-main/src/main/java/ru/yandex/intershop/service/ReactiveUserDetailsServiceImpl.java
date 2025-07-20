package ru.yandex.intershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.intershop.model.User;
import ru.yandex.intershop.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(createUserNotFoundException(username)))
                .map(this::mapToUserDetails);
    }

    private UserDetails mapToUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRoles().toArray(String[]::new))
                .build();
    }

    private UsernameNotFoundException createUserNotFoundException(String username) {
        return new UsernameNotFoundException("Пользователь не найден: " + username);
    }
}
