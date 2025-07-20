package ru.yandex.intershop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import ru.yandex.intershop.dto.ItemDto;
import ru.yandex.intershop.dto.UpdateCartForm;
import ru.yandex.intershop.mapper.ItemToItemDtoMapper;
import ru.yandex.intershop.model.Cart;
import ru.yandex.intershop.model.CartItem;
import ru.yandex.intershop.model.Item;
import ru.yandex.intershop.service.CartService;
import ru.yandex.intershop.service.ItemService;

@Slf4j
@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemsController {

    private final ItemService itemService;
    private final CartService cartService;

    @GetMapping("/{id}")
    public Mono<String> getItem(@PathVariable Long id, Model model) {
        return fetchItemAndCart(id)
                .flatMap(tuple -> prepareItemView(tuple.getT1(), tuple.getT2(), model)
                        .thenReturn("item"));
    }

    @PostMapping("/{id}")
    public Mono<String> updateItem(@PathVariable Long id, @ModelAttribute UpdateCartForm form) {
        return cartService.updateCartItem(id, form.getAction())
                .then(Mono.just("redirect:/items/" + id));
    }

    private Mono<Tuple2<Item, Cart>> fetchItemAndCart(Long itemId) {
        return itemService.getItemById(itemId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Item not found for id: " + itemId)))
                .zipWith(cartService.getCurrentUserCart()
                        //для анонимных пользователей
                        .switchIfEmpty(Mono.just(new Cart())))
                .doOnNext(tuple -> log.info("Fetched item: {}, cart: {}", tuple.getT1(), tuple.getT2()));
    }

    private Mono<Void> prepareItemView(Item item, Cart cart, Model model) {
        return getItemQuantityInCart(item.getId(), cart)
                .doOnNext(quantity -> {
                    ItemDto itemDto = ItemToItemDtoMapper.map(item, quantity);
                    model.addAttribute("item", itemDto);
                })
                .then();
    }

    private Mono<Integer> getItemQuantityInCart(Long itemId, Cart cart) {
        return Flux.fromIterable(cart.getItems())
                .filter(cartItem -> cartItem.getItem().getId().equals(itemId))
                .map(CartItem::getQuantity)
                .next()
                .defaultIfEmpty(0);
    }

}