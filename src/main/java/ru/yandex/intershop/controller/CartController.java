package ru.yandex.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.intershop.dto.UpdateCartForm;
import ru.yandex.intershop.model.Cart;
import ru.yandex.intershop.model.Item;
import ru.yandex.intershop.service.CartService;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/cart/items")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

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
        return transformCartItems(cart)
                .collectList()
                .flatMap(items -> cartService.calculateTotal(cart)
                        .doOnNext(total -> addAttributesToModel(cart, items, total, model))
                        .then());
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