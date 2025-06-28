//package ru.yandex.intershop.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import ru.yandex.intershop.service.OrderService;
//
//@Controller
//@RequestMapping("/orders")
//@RequiredArgsConstructor
//public class OrderController {
//    private final OrderService orderService;
//
//    @GetMapping
//    public String showOrders(Model model) {
//        model.addAttribute("orders", orderService.getAllOrders());
//        return "orders";
//    }
//
//    @GetMapping("/{id}")
//    public String showOrder(
//            @PathVariable Long id,
//            @RequestParam(defaultValue = "false") boolean newOrder,
//            Model model
//    ) {
//        model.addAttribute("order", orderService.getOrderById(id));
//        model.addAttribute("newOrder", newOrder);
//        return "order";
//    }
//
//
//}