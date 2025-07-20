package ru.yandex.intershop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.intershop.client.PaymentClient;
import ru.yandex.intershop.dto.UpdateCartForm;
import ru.yandex.intershop.exception.PaymentServiceUnavailableException;
import ru.yandex.intershop.model.Cart;
import ru.yandex.intershop.model.Item;
import ru.yandex.intershop.service.CartService;

import java.math.BigDecimal;

@Slf4j
@Controller
@RequestMapping("/cart/items")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final PaymentClient paymentClient;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Mono<String> showCart(Model model) {
        return cartService.getCurrentUserCart()
                .flatMap(cart -> prepareCartView(cart, model)
                        .thenReturn("cart"));
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Mono<String> updateCartItem(
            @PathVariable Long id,
            @ModelAttribute UpdateCartForm form) {
        return cartService.updateCartItem(id, form.getAction())
                .then(Mono.just("redirect:/cart/items"));
    }

    private Mono<Void> prepareCartView(Cart cart, Model model) {
        return cartService.calculateTotal(cart)
                .flatMap(total -> prepareModelAttributes(cart, total, model))
                .then();
    }

    private Mono<Void> prepareModelAttributes(Cart cart, BigDecimal total, Model model) {
        boolean isCartEmpty = cart.getItems().isEmpty();
        model.addAttribute("isEmptyCart", isCartEmpty);
        model.addAttribute("total", total);

        if (isCartEmpty || total.compareTo(BigDecimal.ZERO) == 0) {
            return Mono.empty();
        }

        return prepareItemsAttributes(cart, total, model);
    }

    private Mono<Void> prepareItemsAttributes(Cart cart, BigDecimal total, Model model) {
        return transformCartItems(cart)
                .collectList()
                .flatMap(items -> {
                    model.addAttribute("items", items);
                    return checkUserBalanceWithFallback(cart.getUserId(), total, model);
                })
                .then();
    }

    private Mono<Boolean> checkUserBalanceWithFallback(Long userId, BigDecimal total, Model model) {
        model.addAttribute("paymentServiceAvailable", true);
        return checkUserBalance(userId, total, model)
                .flatMap(hasEnoughFunds -> {
                    model.addAttribute("hasEnoughFunds", hasEnoughFunds);
                    return Mono.just(hasEnoughFunds);
                });
    }

    private Mono<Boolean> handlePaymentServiceError(Model model, Throwable error) {
        if (error instanceof PaymentServiceUnavailableException) {
            model.addAttribute("paymentServiceAvailable", false);
        }
        return Mono.just(false);
    }


    private Mono<Boolean> checkUserBalance(Long userId, BigDecimal requiredAmount, Model model) {
        return paymentClient.getUserBalance(userId)
                .doOnError(e -> log.error("Balance check failed for user {}", userId, e))
                .map(balance -> {
                    log.debug("Comparing balance {} with required {}", balance, requiredAmount);
                    return balance.compareTo(requiredAmount) >= 0;
                })
                .defaultIfEmpty(false)
                .onErrorResume(e -> handlePaymentServiceError(model, e));
    }

    private Flux<Item> transformCartItems(Cart cart) {
        return Flux.fromIterable(cart.getItems())
                .map(cartItem -> {
                    Item item = cartItem.getItem();
                    item.setCount(cartItem.getQuantity());
                    return item;
                });
    }
}