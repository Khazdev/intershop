//package ru.yandex.intershop.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import ru.yandex.intershop.enums.ActionType;
//import ru.yandex.intershop.model.Cart;
//import ru.yandex.intershop.model.Item;
//import ru.yandex.intershop.service.CartService;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Controller
//@RequestMapping("/cart/items")
//@RequiredArgsConstructor
//public class CartController {
//    private final CartService cartService;
//
//    @GetMapping
//    public String showCart(Model model) {
//        Cart cart = cartService.getCurrentUserCart();
//        List<Item> items = cart.getItems().stream().map(cartItem -> {
//            Item item = cartItem.getItem();
//            item.setCount(cartItem.getQuantity());
//            return item;
//        }).collect(Collectors.toList());
//        model.addAttribute("items", items);
//        model.addAttribute("total", cartService.calculateTotal(cart));
//        model.addAttribute("empty", cart.getItems().isEmpty());
//        return "cart";
//    }
//
//    @PostMapping("/{id}")
//    public String updateCartItem(
//            @PathVariable Long id,
//            @RequestParam ActionType action
//    ) {
//        cartService.updateCartItem(id, action);
//        return "redirect:/cart/items";
//    }
//}