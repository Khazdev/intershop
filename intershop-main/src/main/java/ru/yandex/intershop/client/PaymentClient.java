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
import ru.yandex.intershop.client.payment.model.PaymentRequest;
import ru.yandex.intershop.client.payment.model.PaymentResponse;
import ru.yandex.intershop.exception.PaymentServiceException;
import ru.yandex.intershop.exception.PaymentServiceUnavailableException;
import ru.yandex.intershop.exception.UserNotFoundException;

import java.math.BigDecimal;
import java.util.Objects;

@Component
public class PaymentClient {

    private static final String PAYMENTS_ENDPOINT = "/payments";
    private static final String BALANCE_ENDPOINT = "/payments/balance/{userId}";
    private final WebClient webClient;

    public PaymentClient(WebClient.Builder webClientBuilder,
                         @Value("${payment.service.url}") String paymentServiceUrl) {
        this.webClient = webClientBuilder
                .baseUrl(paymentServiceUrl)
                .build();
    }

    public Mono<PaymentResponse> processPayment(PaymentRequest request) {
        if (Objects.isNull(request)) {
            return Mono.error(new IllegalArgumentException("Запрос на оплату не может быть null"));
        }

        return webClient.post()
                .uri(PAYMENTS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals, this::handleUnprocessableEntity)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleServiceUnavailable)
                .bodyToMono(PaymentResponse.class)
                .onErrorMap(this::mapToPaymentUnavailableException);
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

    private Mono<Throwable> handleUnprocessableEntity(ClientResponse response) {
        return Mono.error(new PaymentServiceException("Недостаточно средств на балансе"));
    }

    private Mono<Throwable> handleUserNotFound(ClientResponse response) {
        return Mono.error(new UserNotFoundException("Пользователь не найден"));
    }

    private Mono<Throwable> handleServiceUnavailable(ClientResponse response) {
        return Mono.error(new PaymentServiceUnavailableException("Платёжный сервис временно недоступен"));
    }

    private Throwable mapToPaymentUnavailableException(Throwable throwable) {
        return new PaymentServiceUnavailableException("Ошибка соединения с платёжным сервисом: " + throwable.getMessage());
    }

    private Mono<BigDecimal> handleBalanceError(Throwable e) {
        if (e instanceof UserNotFoundException) {
            // Если пользователь не найден, возвращаем 0
            return Mono.just(BigDecimal.ZERO);
        }
        return Mono.error(new PaymentServiceUnavailableException("Не удалось получить баланс пользователя: " + e.getMessage()));
    }
}
