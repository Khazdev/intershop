package ru.yandex.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.intershop.dto.ItemDto;
import ru.yandex.intershop.enums.ActionType;
import ru.yandex.intershop.mapper.ItemToItemDtoMapper;
import ru.yandex.intershop.model.Cart;
import ru.yandex.intershop.model.CartItem;
import ru.yandex.intershop.model.Item;
import ru.yandex.intershop.model.Paging;
import ru.yandex.intershop.service.CartService;
import ru.yandex.intershop.service.ItemService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ItemService itemService;
    private final CartService cartService;

    @GetMapping("/")
    public String root() {
        return "redirect:/main/items";
    }

    @GetMapping("/main/items")
    public String showItems(Model model) {
        List<Item> allExistingItems = itemService.findItems();
        Cart currentUserCart = cartService.getCurrentUserCart();
        List<ItemDto> items = ItemToItemDtoMapper.mapList(allExistingItems, currentUserCart);

        model.addAttribute("items", List.of(items));
        model.addAttribute("search", "");
        model.addAttribute("sort", "NO");
        model.addAttribute("paging", new Paging(1, 1, false, false));

        return "main";
    }

    @PostMapping("/main/items/{id}")
    public String updateMainCart(@PathVariable Long id, @RequestParam ActionType action) {
        cartService.updateCartItem(id, action);
        return "redirect:/main/items";
    }

}