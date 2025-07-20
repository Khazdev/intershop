package ru.yandex.intershop.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.yandex.intershop.model.User;
import ru.yandex.intershop.repository.UserRepository;

import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        userRepository.count()
                .filter(count -> count == 0)
                .flatMap(count -> {
                    User admin = new User();
                    admin.setUsername("admin");
                    admin.setPassword(passwordEncoder.encode("admin"));
                    admin.setRoles(Set.of("ADMIN", "NOTUSER"));

                    User user = new User();
                    user.setUsername("user");
                    user.setPassword(passwordEncoder.encode("user"));
                    user.setRoles(Set.of("USER"));

                    User notuser = new User();
                    notuser.setUsername("notuser");
                    notuser.setPassword(passwordEncoder.encode("notuser"));
                    notuser.setRoles(Set.of("NOTUSER"));

                    return userRepository.saveAll(List.of(admin, user, notuser)).then();
                })
                .subscribe();
    }
}