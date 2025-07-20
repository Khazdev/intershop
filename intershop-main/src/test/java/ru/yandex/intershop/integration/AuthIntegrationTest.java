package ru.yandex.intershop.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.intershop.model.User;
import ru.yandex.intershop.repository.UserRepository;

@SpringBootTest
@AutoConfigureWebTestClient
class AuthIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Test
    void anonymousUser_AccessMainPage_Success() {
        webTestClient.get().uri("/main/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void anonymousUser_AccessOrders_RedirectToLogin() {
        webTestClient.get().uri("/orders")
                .exchange()
                .expectStatus().isFound()
                .expectHeader().location("/login");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void authenticatedUser_AccessOrders_Success() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        userRepository.save(user).block();

        webTestClient.get().uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }
}