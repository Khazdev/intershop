package ru.yandex.intershop.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.yandex.intershop.payment.configuration.PaymentConfig;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BalanceService {
    private final PaymentConfig paymentConfig;

    public Mono<BigDecimal> getBalance(Long userId) {
        return Mono.justOrEmpty(
                paymentConfig.getBalances().stream()
                        .filter(b -> b.userId().equals(userId))
                        .findFirst()
                        .map(PaymentConfig.Balance::amount)
        ).switchIfEmpty(Mono.error(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден")
        ));
    }
}