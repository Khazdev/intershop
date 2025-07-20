package ru.yandex.intershop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
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

@Slf4j
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
                                  Model model, ServerWebExchange exchange) {
        return exchange.getSession()
                .doOnNext(session -> log.info("ID сессии: {}", session.getId()))
                .then(fetchItemsAndCart(search, sort, pageNumber, pageSize)
                .flatMap(tuple -> prepareItemsView(tuple.getT1(), tuple.getT2(), search, sort, model))
                .thenReturn("main"));
    }

    @PostMapping("/main/items/{id}")
    public Mono<String> updateMainCart(@PathVariable Long id, @ModelAttribute UpdateCartForm form) {
        return cartService.updateCartItem(id, form.getAction())
                .then(Mono.just("redirect:/main/items"));
    }

    @PostMapping("/buy")
    public Mono<String> buy() {
        return orderService.createOrderFromCart()
                .map(order -> "redirect:/orders/" + order.getId() + "?newOrder=true");
    }

    private Mono<Tuple2<Page<Item>, Cart>> fetchItemsAndCart(String search, SortType sort, int pageNumber, int pageSize) {
        return itemService.findItems(search, sort, pageNumber, pageSize)
                .zipWith(cartService.getCurrentUserCart()
                .switchIfEmpty(Mono.just(new Cart())));
    }

    private Mono<Void> prepareItemsView(Page<Item> itemPage, Cart cart, String search, SortType sort, Model model) {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication() != null
                        && securityContext.getAuthentication().isAuthenticated()
                        && !"anonymousUser".equals(securityContext.getAuthentication().getPrincipal()))
                .doOnNext(isAuthenticated -> log.info("isAuthenticated: {}", isAuthenticated))
                .flatMap(isAuthenticated -> {
                    Mono<List<ItemDto>> itemDtosMono = cart.getId() != null
                            ? ItemToItemDtoMapper.mapList(Flux.fromIterable(itemPage.getContent()), Mono.just(cart))
                            : ItemToItemDtoMapper.mapList(Flux.fromIterable(itemPage.getContent()), Mono.just(new Cart()));

                    return itemDtosMono
                            .flatMap(itemDtos -> splitIntoRows(itemDtos, 4)
                                    .collectList()
                                    .doOnNext(rows -> addAttributesToModel(itemPage, rows, search, sort, isAuthenticated, cart, model))
                                    .then());
                });
    }

    private void addAttributesToModel(Page<Item> itemPage, List<List<ItemDto>> rows, String search, SortType sort,
                                      boolean isAuthenticated, Cart cart, Model model) {
        Paging paging = new Paging(itemPage.getNumber() + 1, itemPage.getSize(),
                itemPage.hasNext(), itemPage.hasPrevious());
        model.addAttribute("items", rows);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort.name().toUpperCase(Locale.ROOT));
        model.addAttribute("paging", paging);
        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("cart", cart.getId() != null ? cart : null);
    }

    public Flux<List<ItemDto>> splitIntoRows(List<ItemDto> items, int itemsPerRow) {
        if (items == null || itemsPerRow <= 0) {
            return Flux.empty();
        }
        return Flux.range(0, calculateRowCount(items.size(), itemsPerRow))
                .map(rowIndex -> getRowItems(items, rowIndex, itemsPerRow));
    }

    private int calculateRowCount(int totalItems, int itemsPerRow) {
        return (totalItems + itemsPerRow - 1) / itemsPerRow;
    }

    private List<ItemDto> getRowItems(List<ItemDto> items, int rowIndex, int itemsPerRow) {
        int startIndex = rowIndex * itemsPerRow;
        int endIndex = Math.min(startIndex + itemsPerRow, items.size());
        return items.subList(startIndex, endIndex);
    }
}