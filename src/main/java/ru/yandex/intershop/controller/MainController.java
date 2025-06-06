package ru.yandex.intershop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.yandex.intershop.model.Paging;

import java.util.Collections;

@Controller
public class MainController {

    @GetMapping("/")
    public String root() {
        return "redirect:/main/items";
    }

    @GetMapping("/main/items")
    public String showItems(Model model) {

        model.addAttribute("items", Collections.EMPTY_LIST);
        model.addAttribute("search", "");
        model.addAttribute("sort", "NO");
        model.addAttribute("paging", new Paging(1, 1, false, false));

        return "main";
    }

}