package ru.yandex.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.yandex.intershop.dto.ItemDto;
import ru.yandex.intershop.dto.UpdateCartForm;
import ru.yandex.intershop.enums.SortType;
import ru.yandex.intershop.mapper.ItemToItemDtoMapper;
import ru.yandex.intershop.model.Cart;
import ru.yandex.intershop.model.Item;
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
    public Mono<String> root() {
        return Mono.just("redirect:/main/items");
    }

    @GetMapping("/main/items")
    public Mono<String> showItems(@RequestParam(defaultValue = "") String search,
                                  @RequestParam(defaultValue = "NO") SortType sort,
                                  @RequestParam(defaultValue = "10") int pageSize,
                                  @RequestParam(defaultValue = "1") int pageNumber,
                                  Model model) {
        return itemService.findItems(search, sort, pageNumber, pageSize)
                .zipWith(cartService.getCurrentUserCart())
                .map(tuple -> {
                    List<Item> items = tuple.getT1().getContent();
                    Cart currentUserCart = tuple.getT2();
                    List<ItemDto> itemDtos = ItemToItemDtoMapper.mapList(items, currentUserCart);
                    List<List<ItemDto>> result = splitIntoRows(itemDtos, 4);
                    Paging paging = new Paging(tuple.getT1().getNumber() + 1, tuple.getT1().getSize(),
                            tuple.getT1().hasNext(), tuple.getT1().hasPrevious());

                    model.addAttribute("items", result);
                    model.addAttribute("search", search);
                    model.addAttribute("sort", sort.name().toUpperCase(Locale.ROOT));
                    model.addAttribute("paging", paging);

                    return "main";
                });
    }

    @PostMapping("/main/items/{id}")
    public Mono<String> updateMainCart(@PathVariable Long id, @ModelAttribute UpdateCartForm form) {
        return cartService.updateCartItem(id, form.getAction())
                .then(Mono.just("redirect:/main/items"));
    }

    @PostMapping("/buy")
    public String buy() {
//        Order order = orderService.createOrderFromCart();
//        return "redirect:/orders/" + order.getId() + "?newOrder=true";
        return "";
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