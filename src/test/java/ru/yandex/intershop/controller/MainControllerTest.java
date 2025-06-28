//package ru.yandex.intershop.controller;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.test.web.servlet.MockMvc;
//import ru.yandex.intershop.configuration.TestConfig;
//import ru.yandex.intershop.enums.ActionType;
//import ru.yandex.intershop.enums.SortType;
//import ru.yandex.intershop.model.Cart;
//import ru.yandex.intershop.model.Item;
//import ru.yandex.intershop.model.Order;
//import ru.yandex.intershop.model.Paging;
//import ru.yandex.intershop.service.CartService;
//import ru.yandex.intershop.service.ItemService;
//import ru.yandex.intershop.service.OrderService;
//
//import java.util.List;
//
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(MainController.class)
//@Import(TestConfig.class)
//class MainControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ItemService itemService;
//
//    @Autowired
//    private CartService cartService;
//
//    @Autowired
//    private OrderService orderService;
//
//    @BeforeEach
//    void setUp() {
//        reset(itemService, cartService, orderService);
//    }
//
//    @Test
//    void root_redirectsToMainItems() throws Exception {
//        mockMvc.perform(get("/"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/main/items"));
//    }
//
//    @Test
//    void showItems_withDefaultParams_returnsMainView() throws Exception {
//        Item item = new Item();
//        Page<Item> page = new PageImpl<>(List.of(item));
//        Cart cart = new Cart();
//        when(itemService.findItems("", SortType.NO, 1, 10)).thenReturn(page);
//        when(cartService.getCurrentUserCart()).thenReturn(cart);
//
//        mockMvc.perform(get("/main/items"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("main"))
//                .andExpect(model().attributeExists("items"))
//                .andExpect(model().attributeExists("search"))
//                .andExpect(model().attributeExists("sort"))
//                .andExpect(model().attributeExists("paging"))
//                .andExpect(model().attribute("search", ""))
//                .andExpect(model().attribute("sort", "NO"));
//
//        verify(itemService).findItems("", SortType.NO, 1, 10);
//        verify(cartService).getCurrentUserCart();
//    }
//
//    @Test
//    void showItems_withSearchAndSort_returnsMainViewWithCorrectAttributes() throws Exception {
//        Item item = new Item();
//        Page<Item> page = new PageImpl<>(List.of(item));
//        Cart cart = new Cart();
//        when(itemService.findItems("test", SortType.ALPHA, 2, 5)).thenReturn(page);
//        when(cartService.getCurrentUserCart()).thenReturn(cart);
//
//        mockMvc.perform(get("/main/items")
//                        .param("search", "test")
//                        .param("sort", "ALPHA")
//                        .param("pageNumber", "2")
//                        .param("pageSize", "5"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("main"))
//                .andExpect(model().attribute("search", "test"))
//                .andExpect(model().attribute("sort", "ALPHA"))
//                .andExpect(model().attribute("paging", new Paging(1, 1, false, false)));
//
//        verify(itemService).findItems("test", SortType.ALPHA, 2, 5);
//    }
//
//    @Test
//    void updateMainCart_updatesCartAndRedirects() throws Exception {
//        doNothing().when(cartService).updateCartItem(1L, ActionType.PLUS);
//
//        mockMvc.perform(post("/main/items/1")
//                        .param("action", "PLUS"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/main/items"));
//
//        verify(cartService).updateCartItem(1L, ActionType.PLUS);
//    }
//
//    @Test
//    void buy_createsOrderAndRedirects() throws Exception {
//        Order order = new Order();
//        order.setId(1L);
//        when(orderService.createOrderFromCart()).thenReturn(order);
//
//        mockMvc.perform(post("/buy"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/orders/1?newOrder=true"));
//
//        verify(orderService).createOrderFromCart();
//    }
//}
