package ru.yandex.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.intershop.dto.ItemDto;
import ru.yandex.intershop.enums.ActionType;
import ru.yandex.intershop.enums.SortType;
import ru.yandex.intershop.mapper.ItemToItemDtoMapper;
import ru.yandex.intershop.model.Cart;
import ru.yandex.intershop.model.Item;
import ru.yandex.intershop.model.Order;
import ru.yandex.intershop.model.Paging;
import ru.yandex.intershop.service.CartService;
import ru.yandex.intershop.service.ItemService;
import ru.yandex.intershop.service.OrderService;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ItemService itemService;
    private final CartService cartService;
    private final OrderService orderService;

    @GetMapping("/")
    public String root() {
        return "redirect:/main/items";
    }

    @GetMapping("/main/items")
    public String showItems(@RequestParam(defaultValue = "") String search,
                            @RequestParam(defaultValue = "NO") SortType sort,
                            @RequestParam(defaultValue = "10") int pageSize,
                            @RequestParam(defaultValue = "1") int pageNumber,
                            Model model) {

        Page<Item> page = itemService.findItems(search, sort, pageNumber, pageSize);
        Cart currentUserCart = cartService.getCurrentUserCart();
        List<ItemDto> items = ItemToItemDtoMapper.mapList(page.getContent(), currentUserCart);
        List<List<ItemDto>> result = splitIntoRows(items, 4);
        Paging paging = new Paging(page.getNumber() + 1, page.getSize(), page.hasNext(), page.hasPrevious());

        model.addAttribute("items", result);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort.name().toUpperCase(Locale.ROOT));
        model.addAttribute("paging", paging);

        return "main";
    }

    @PostMapping("/main/items/{id}")
    public String updateMainCart(@PathVariable Long id, @RequestParam ActionType action) {
        cartService.updateCartItem(id, action);
        return "redirect:/main/items";
    }

    @PostMapping("/buy")
    public String buy() {
        Order order = orderService.createOrderFromCart();
        return "redirect:/orders/" + order.getId() + "?newOrder=true";
    }

    private List<List<ItemDto>> splitIntoRows(List<ItemDto> items, int itemsPerRow) {
        return IntStream.range(0, (items.size() + itemsPerRow - 1) / itemsPerRow)
                .mapToObj(i -> items.subList(
                        i * itemsPerRow,
                        Math.min((i + 1) * itemsPerRow, items.size())
                ))
                .collect(Collectors.toList());
    }
}