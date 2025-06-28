package ru.yandex.intershop.controller;

import lombok.RequiredArgsConstructor;
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


@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemsController {

    private final ItemService itemService;
    private final CartService cartService;

    @GetMapping("/{id}")
    public Mono<String> getItem(@PathVariable Long id, Model model) {
        return Mono.zip(itemService.getItemById(id), cartService.getCurrentUserCart())
                .map(tuple -> {
                    Item item = tuple.getT1();
                    Cart currentUserCart = tuple.getT2();
                    int count = currentUserCart.getItems().stream()
                            .filter(cartItem -> cartItem.getItem().getId().equals(id))
                            .map(CartItem::getQuantity)
                            .findFirst()
                            .orElse(0);
                    ItemDto itemDto = ItemToItemDtoMapper.map(item, count);
                    model.addAttribute("item", itemDto);
                    return "item";
                });
    }

    @PostMapping("/{id}")
    public Mono<String> updateItem(@PathVariable Long id, @ModelAttribute UpdateCartForm form) {
        return cartService.updateCartItem(id, form.getAction())
                .then(Mono.just("redirect:/items/" + id));
    }

}