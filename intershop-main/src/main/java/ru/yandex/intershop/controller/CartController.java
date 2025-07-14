package ru.yandex.intershop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/cart/items")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final PaymentClient paymentClient;

    @GetMapping
    public Mono<String> showCart(Model model) {
        return cartService.getCurrentUserCart()
                .flatMap(cart -> prepareCartView(cart, model)
                        .thenReturn("cart"));
    }

    @PostMapping("/{id}")
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
        Mono<Boolean> balanceCheck = checkUserBalance(cart.getUserId(), total, model)
                .onErrorResume(e -> handlePaymentServiceError(model, e));

        Mono<List<Item>> itemsMono = transformCartItems(cart).collectList();

        return Mono.zip(itemsMono, balanceCheck)
                .doOnNext(tuple -> {
                    List<Item> items = tuple.getT1();
                    boolean hasEnoughFunds = tuple.getT2();

                    addAttributesToModel(cart, items, total, model);
                    if (!model.containsAttribute("paymentServiceAvailable")) {
                        model.addAttribute("paymentServiceAvailable", true);
                        model.addAttribute("hasEnoughFunds", hasEnoughFunds);
                    }

                })
                .then();
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

    private void addAttributesToModel(Cart cart, List<Item> items, BigDecimal total, Model model) {
        model.addAttribute("items", items);
        model.addAttribute("total", total);
        model.addAttribute("empty", cart.getItems().isEmpty());
    }
}