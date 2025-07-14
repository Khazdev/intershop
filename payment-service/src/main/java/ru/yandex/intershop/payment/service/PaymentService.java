package ru.yandex.intershop.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.yandex.intershop.payment.model.PaymentRequest;
import ru.yandex.intershop.payment.model.PaymentResponse;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final BalanceService balanceService;

    public Mono<PaymentResponse> processPayment(PaymentRequest request) {

        return balanceService.getBalance(request.getUserId())
                .flatMap(balance -> {
                    if (balance.compareTo(request.getAmount()) >= 0) {
                        return balanceService.deductBalance(request.getUserId(), request.getAmount())
                                .thenReturn(new PaymentResponse()
                                        .transactionId(UUID.randomUUID())
                                        .status(PaymentResponse.StatusEnum.SUCCESS)
                                        .message("Платеж обработан"));
                    } else {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.UNPROCESSABLE_ENTITY,
                                "Недостаточно средств"
                        ));
                    }
                });
    }
}