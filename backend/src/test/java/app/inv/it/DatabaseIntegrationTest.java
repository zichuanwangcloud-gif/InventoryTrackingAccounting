package app.inv.it;

import app.inv.entity.Category;
import app.inv.entity.Item;
import app.inv.entity.User;
import app.inv.repository.CategoryRepository;
import app.inv.repository.ItemRepository;
import app.inv.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integration-test")
class DatabaseIntegrationTest {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        itemRepository.deleteAll();
        userRepository.deleteAll();
        
        // 创建测试用户
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedpassword");
        testUser = userRepository.save(testUser);

        // 获取默认分类
        List<Category> categories = categoryRepository.findAll();
        assertThat(categories).isNotEmpty();
        testCategory = categories.get(0);
    }

    @Test
    void shouldCreateAndRetrieveUser() {
        // Given
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPasswordHash("password");

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        Optional<User> foundUser = userRepository.findByUsernameOrEmail("newuser");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("newuser");
        assertThat(foundUser.get().getEmail()).isEqualTo("new@example.com");
    }

    @Test
    @Transactional
    void shouldCreateAndRetrieveItem() {
        // Given
        Item item = new Item();
        item.setUser(testUser);
        item.setName("测试物品");
        item.setCategory(testCategory);
        item.setBrand("测试品牌");
        item.setSize("M");
        item.setColor("蓝色");
        item.setPurchasePrice(new BigDecimal("100.00"));
        item.setPurchaseDate(LocalDate.now());
        item.setLocation("仓库A");
        item.setStatus(Item.ItemStatus.ACTIVE);

        // When
        Item savedItem = itemRepository.save(item);

        // Then
        Optional<Item> foundItem = itemRepository.findById(savedItem.getId());
        assertThat(foundItem).isPresent();
        assertThat(foundItem.get().getName()).isEqualTo("测试物品");
        assertThat(foundItem.get().getUser().getUsername()).isEqualTo("testuser");
        assertThat(foundItem.get().getCategory().getName()).isEqualTo(testCategory.getName());
    }

    @Test
    void shouldRetrieveItemsByUser() {
        // Given
        Item item1 = new Item();
        item1.setUser(testUser);
        item1.setName("物品1");
        item1.setCategory(testCategory);
        item1.setPurchasePrice(new BigDecimal("100.00"));
        item1.setPurchaseDate(LocalDate.now());
        item1.setStatus(Item.ItemStatus.ACTIVE);
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setUser(testUser);
        item2.setName("物品2");
        item2.setCategory(testCategory);
        item2.setPurchasePrice(new BigDecimal("200.00"));
        item2.setPurchaseDate(LocalDate.now());
        item2.setStatus(Item.ItemStatus.ACTIVE);
        itemRepository.save(item2);

        // When
        List<Item> userItems = itemRepository.findByUserAndDeletedAtIsNull(testUser, null).getContent();

        // Then
        assertThat(userItems).hasSize(2);
        assertThat(userItems).extracting(Item::getName).containsExactlyInAnyOrder("物品1", "物品2");
    }

    @Test
    void shouldRetrieveCategories() {
        // When
        List<Category> categories = categoryRepository.findAll();

        // Then
        assertThat(categories).isNotEmpty();
        assertThat(categories).extracting(Category::getName).contains("服装", "鞋子", "配饰", "其他");
    }
}
