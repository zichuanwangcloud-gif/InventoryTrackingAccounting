package app.inv.controller;

import app.inv.dto.ApiResponse;
import app.inv.dto.ItemRequest;
import app.inv.entity.Item;
import app.inv.entity.User;
import app.inv.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {
    
    private final ItemService itemService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Item>>> getItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Item.ItemStatus status) {
        
        // TODO: 从JWT Token中获取当前用户
        User currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<Item> items = itemService.getItemsByUserWithFilters(currentUser, search, categoryId, status, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(items));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Item>> getItem(@PathVariable UUID id) {
        // TODO: 从JWT Token中获取当前用户并验证权限
        Item item = itemService.getItemById(id)
                .orElseThrow(() -> new RuntimeException("物品不存在"));
        
        return ResponseEntity.ok(ApiResponse.success(item));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<Item>> createItem(@Valid @RequestBody ItemRequest request) {
        try {
            // TODO: 从JWT Token中获取当前用户
            User currentUser = new User();
            currentUser.setId(UUID.randomUUID());
            
            Item item = itemService.createItem(
                    currentUser,
                    request.getName(),
                    request.getPurchasePrice(),
                    request.getPurchaseDate(),
                    request.getBrand(),
                    request.getSize(),
                    request.getColor(),
                    request.getLocation(),
                    request.getImages()
            );
            
            return ResponseEntity.ok(ApiResponse.success("物品创建成功", item));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Item>> updateItem(@PathVariable UUID id, 
                                                       @Valid @RequestBody ItemRequest request) {
        try {
            Item item = itemService.updateItem(
                    id,
                    request.getName(),
                    request.getPurchasePrice(),
                    request.getPurchaseDate(),
                    request.getBrand(),
                    request.getSize(),
                    request.getColor(),
                    request.getLocation(),
                    request.getImages()
            );
            
            return ResponseEntity.ok(ApiResponse.success("物品更新成功", item));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable UUID id) {
        try {
            itemService.deleteItem(id);
            return ResponseEntity.ok(ApiResponse.success("物品删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getItemStats() {
        // TODO: 从JWT Token中获取当前用户
        User currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalValue", itemService.getTotalValueByUser(currentUser));
        stats.put("categoryValues", itemService.getValueByCategory(currentUser));
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
