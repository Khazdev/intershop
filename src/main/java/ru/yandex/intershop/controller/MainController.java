package ru.yandex.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.yandex.intershop.model.Item;
import ru.yandex.intershop.model.Paging;

import ru.yandex.intershop.service.ItemService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ItemService itemService;

    @GetMapping("/")
    public String root() {
        return "redirect:/main/items";
    }

    @GetMapping("/main/items")
    public String showItems(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", List.of(items, items));
        model.addAttribute("search", "");
        model.addAttribute("sort", "NO");
        model.addAttribute("paging", new Paging(1, 1, false, false));

        return "main";
    }

}