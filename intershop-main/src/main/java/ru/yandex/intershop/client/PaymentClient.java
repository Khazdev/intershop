package ru.yandex.intershop.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.intershop.client.payment.model.BalanceResponse;
import ru.yandex.intershop.exception.PaymentServiceException;
import ru.yandex.intershop.exception.PaymentServiceUnavailableException;
import ru.yandex.intershop.exception.UserNotFoundException;

import java.math.BigDecimal;

@Component
public class PaymentClient {

    private static final String BALANCE_ENDPOINT = "/payments/balance/{userId}";
    private final WebClient webClient;

    public PaymentClient(WebClient.Builder webClientBuilder,
                         @Value("${payment.service.url}") String paymentServiceUrl) {
        this.webClient = webClientBuilder
                .baseUrl(paymentServiceUrl)
                .build();
    }

    public Mono<BigDecimal> getUserBalance(Long userId) {
        return webClient.get()
                .uri(BALANCE_ENDPOINT, userId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, this::handleUserNotFound)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleServiceUnavailable)
                .bodyToMono(BalanceResponse.class)
                .map(BalanceResponse::getAmount)
                .onErrorResume(this::handleBalanceError);
    }

    private Mono<Throwable> handleUserNotFound(ClientResponse response) {
        return Mono.error(new UserNotFoundException("Пользователь не найден"));
    }

    private Mono<Throwable> handleServiceUnavailable(ClientResponse response) {
        return Mono.error(new PaymentServiceUnavailableException("Платёжный сервис временно недоступен"));
    }

    private Mono<BigDecimal> handleBalanceError(Throwable e) {
        if (e instanceof UserNotFoundException) {
            // Если пользователь не найден, возвращаем 0
            return Mono.just(BigDecimal.ZERO);
        }
        return Mono.error(new PaymentServiceException("Не удалось получить баланс пользователя: " + e.getMessage()));
    }
}
