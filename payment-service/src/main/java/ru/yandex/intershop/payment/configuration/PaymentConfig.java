package ru.yandex.intershop.payment.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "payment")
@Getter
@Setter
public class PaymentConfig {
    private List<Balance> balances;

    public record Balance(
            Long userId,
            BigDecimal amount
    ) {}
}