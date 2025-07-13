package ru.yandex.intershop.payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.intershop.payment.api.PaymentsApi;
import ru.yandex.intershop.payment.model.BalanceResponse;
import ru.yandex.intershop.payment.service.BalanceService;

@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentsApi {

    private final BalanceService balanceService;

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(
            @PathVariable Long userId,
            ServerWebExchange exchange
    ) {
        return balanceService.getBalance(userId)
                .flatMap(balance -> {
                    BalanceResponse response = new BalanceResponse();
                    response.setUserId(userId);
                    response.setAmount(balance);
                    return Mono.just(ResponseEntity.ok(response));
                })
                .onErrorResume(ResponseStatusException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    return Mono.just(ResponseEntity
                            .status(ex.getStatusCode())
                            .body(new BalanceResponse()));
                })
                .onErrorResume(e -> Mono.just(ResponseEntity
                        .internalServerError()
                        .body(new BalanceResponse())));
    }
}