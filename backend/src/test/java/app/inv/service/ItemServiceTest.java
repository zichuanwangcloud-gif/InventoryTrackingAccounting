package app.inv.service;

import app.inv.entity.Item;
import app.inv.entity.User;
import app.inv.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private User testUser;
    private Item testItem;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");

        testItem = new Item();
        testItem.setId(UUID.randomUUID());
        testItem.setName("测试物品");
        testItem.setUser(testUser);
        testItem.setPurchasePrice(new BigDecimal("100.00"));
        testItem.setPurchaseDate(LocalDate.now());
        testItem.setBrand("测试品牌");
        testItem.setSize("M");
        testItem.setColor("蓝色");
        testItem.setLocation("仓库A");
        testItem.setStatus(Item.ItemStatus.ACTIVE);
    }

    @Test
    void createItem_shouldCreateAndSaveItem() {
        // Given
        String name = "新物品";
        BigDecimal purchasePrice = new BigDecimal("200.00");
        LocalDate purchaseDate = LocalDate.now();
        String brand = "品牌";
        String size = "L";
        String color = "红色";
        String location = "仓库B";
        String images = "image1.jpg,image2.jpg";

        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            item.setId(UUID.randomUUID());
            return item;
        });

        // When
        Item result = itemService.createItem(testUser, name, purchasePrice, purchaseDate,
                brand, size, color, location, images);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getPurchasePrice()).isEqualTo(purchasePrice);
        assertThat(result.getPurchaseDate()).isEqualTo(purchaseDate);
        assertThat(result.getBrand()).isEqualTo(brand);
        assertThat(result.getSize()).isEqualTo(size);
        assertThat(result.getColor()).isEqualTo(color);
        assertThat(result.getLocation()).isEqualTo(location);
        assertThat(result.getImages()).isEqualTo(images);
        assertThat(result.getStatus()).isEqualTo(Item.ItemStatus.ACTIVE);

        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void getItemsByUser_shouldReturnPagedItems() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> expectedPage = new PageImpl<>(List.of(testItem), pageable, 1);

        when(itemRepository.findByUserAndDeletedAtIsNull(testUser, pageable)).thenReturn(expectedPage);

        // When
        Page<Item> result = itemService.getItemsByUser(testUser, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testItem);
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(itemRepository).findByUserAndDeletedAtIsNull(testUser, pageable);
    }

    @Test
    void getItemById_shouldReturnItemWhenExists() {
        // Given
        UUID itemId = UUID.randomUUID();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(testItem));

        // When
        Optional<Item> result = itemService.getItemById(itemId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testItem);

        verify(itemRepository).findById(itemId);
    }

    @Test
    void getItemById_shouldReturnEmptyWhenNotExists() {
        // Given
        UUID itemId = UUID.randomUUID();
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // When
        Optional<Item> result = itemService.getItemById(itemId);

        // Then
        assertThat(result).isEmpty();

        verify(itemRepository).findById(itemId);
    }
}
