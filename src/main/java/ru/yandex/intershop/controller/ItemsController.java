package ru.yandex.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.yandex.intershop.dto.ItemDto;
import ru.yandex.intershop.enums.ActionType;
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
    public String getItem(@PathVariable Long id, Model model) {
        Item item = itemService.getItemById(id);
        Cart currentUserCart = cartService.getCurrentUserCart();
        int count = currentUserCart.getItems().stream()
                .filter(cartItem -> cartItem.getItem().getId().equals(id))
                .map(CartItem::getQuantity)
                .findFirst()
                .orElse(0);
        ItemDto itemDto = ItemToItemDtoMapper.map(item, count);
        model.addAttribute("item", itemDto);
        return "item";
    }

    @PostMapping("/{id}")
    public String updateItem(@PathVariable Long id, @RequestParam ActionType action) {
        cartService.updateCartItem(id, action);
        return "redirect:/items/" + id;
    }

}