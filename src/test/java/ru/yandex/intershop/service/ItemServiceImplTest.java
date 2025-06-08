package ru.yandex.intershop.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.yandex.intershop.enums.SortType;
import ru.yandex.intershop.model.Item;
import ru.yandex.intershop.repository.ItemRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void findItems_noSearch_returnsPagedItems() {
        Page<Item> page = new PageImpl<>(List.of(new Item()));
        when(itemRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<Item> result = itemService.findItems("", SortType.NO, 1, 10);

        assertEquals(1, result.getContent().size());
        verify(itemRepository).findAll(any(PageRequest.class));
    }

    @Test
    void findItems_withSearch_returnsFilteredItems() {
        Page<Item> page = new PageImpl<>(List.of(new Item()));
        when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                anyString(), anyString(), any(PageRequest.class))).thenReturn(page);

        Page<Item> result = itemService.findItems("test", SortType.ALPHA, 1, 10);

        assertEquals(1, result.getContent().size());
        verify(itemRepository).findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                eq("test"), eq("test"), any(PageRequest.class));
    }

    @Test
    void getItemById_returnsItem() {
        Item item = new Item();
        when(itemRepository.getItemById(1L)).thenReturn(item);

        Item result = itemService.getItemById(1L);

        assertEquals(item, result);
        verify(itemRepository).getItemById(1L);
    }
}