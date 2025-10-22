package app.inv.controller;

import app.inv.entity.Item;
import app.inv.entity.User;
import app.inv.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import app.inv.config.TestConfig;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
@Import(TestConfig.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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
    @WithMockUser(username = "testuser")
    void getItems_shouldReturnPagedItems() throws Exception {
        // Given
        Page<Item> page = new PageImpl<>(List.of(testItem), PageRequest.of(0, 10), 1);
        when(itemService.getItemsByUser(any(User.class), any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/items")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].name").value("测试物品"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getItemById_shouldReturnItemWhenExists() throws Exception {
        // Given
        UUID itemId = UUID.randomUUID();
        when(itemService.getItemById(itemId)).thenReturn(Optional.of(testItem));

        // When & Then
        mockMvc.perform(get("/api/v1/items/{id}", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("测试物品"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getItemById_shouldReturnNotFoundWhenNotExists() throws Exception {
        // Given
        UUID itemId = UUID.randomUUID();
        when(itemService.getItemById(itemId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/items/{id}", itemId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @WithMockUser(username = "testuser")
    void createItem_shouldCreateItemSuccessfully() throws Exception {
        // Given
        when(itemService.createItem(any(User.class), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(testItem);

        String requestBody = """
                {
                    "name": "新物品",
                    "purchasePrice": 200.00,
                    "purchaseDate": "2024-01-01",
                    "brand": "品牌",
                    "size": "L",
                    "color": "红色",
                    "location": "仓库B",
                    "images": "image1.jpg,image2.jpg"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("物品创建成功"));
    }

    // RED阶段：编写失败测试
    @Test
    @WithMockUser(username = "testuser")
    void updateItem_shouldUpdateItemSuccessfully() throws Exception {
        // Given
        UUID itemId = UUID.randomUUID();
        when(itemService.updateItem(any(UUID.class), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(testItem);

        String requestBody = """
                {
                    "name": "更新的物品",
                    "purchasePrice": 300.00,
                    "purchaseDate": "2024-01-02",
                    "brand": "新品牌",
                    "size": "XL",
                    "color": "绿色",
                    "location": "仓库C",
                    "images": "new_image.jpg"
                }
                """;

        // When & Then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/items/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("物品更新成功"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateItem_shouldFailWhenItemNotFound() throws Exception {
        // Given
        UUID itemId = UUID.randomUUID();
        when(itemService.updateItem(any(UUID.class), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("物品不存在"));

        String requestBody = """
                {
                    "name": "更新的物品",
                    "purchasePrice": 300.00,
                    "purchaseDate": "2024-01-02"
                }
                """;

        // When & Then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/items/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("物品不存在"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteItem_shouldDeleteItemSuccessfully() throws Exception {
        // Given
        UUID itemId = UUID.randomUUID();
        // deleteItem方法返回void，所以不需要mock返回值

        // When & Then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/items/{id}", itemId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("物品删除成功"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteItem_shouldFailWhenItemNotFound() throws Exception {
        // Given
        UUID itemId = UUID.randomUUID();
        org.mockito.Mockito.doThrow(new RuntimeException("物品不存在"))
                .when(itemService).deleteItem(itemId);

        // When & Then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/items/{id}", itemId)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("物品不存在"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getItemStats_shouldReturnStats() throws Exception {
        // Given
        when(itemService.getTotalValueByUser(any(User.class))).thenReturn(1000.0);
        when(itemService.getValueByCategory(any(User.class))).thenReturn(List.of(new Object[]{"服装", 500.0}));

        // When & Then
        mockMvc.perform(get("/api/v1/items/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalValue").value(1000.0));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getItems_shouldSupportSearchAndFilters() throws Exception {
        // Given
        Page<Item> page = new PageImpl<>(List.of(testItem), PageRequest.of(0, 10), 1);
        when(itemService.getItemsByUserWithFilters(any(User.class), any(), any(), any(), any()))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/items")
                        .param("search", "测试")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void createItem_shouldFailWhenValidationFails() throws Exception {
        // Given - 空的请求体
        String invalidRequestBody = """
                {
                    "name": "",
                    "purchasePrice": -100.00,
                    "purchaseDate": "invalid-date"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
