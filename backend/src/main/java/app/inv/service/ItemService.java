package app.inv.service;

import app.inv.entity.Item;
import app.inv.entity.User;
import app.inv.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {
    
    private final ItemRepository itemRepository;
    
    public Item createItem(User user, String name, BigDecimal purchasePrice, LocalDate purchaseDate,
                          String brand, String size, String color, String location, String images) {
        Item item = new Item();
        item.setUser(user);
        item.setName(name);
        item.setPurchasePrice(purchasePrice);
        item.setPurchaseDate(purchaseDate);
        item.setBrand(brand);
        item.setSize(size);
        item.setColor(color);
        item.setLocation(location);
        item.setImages(images);
        item.setStatus(Item.ItemStatus.ACTIVE);
        
        return itemRepository.save(item);
    }
    
    public Page<Item> getItemsByUser(User user, Pageable pageable) {
        return itemRepository.findByUserAndDeletedAtIsNull(user, pageable);
    }
    
    public Page<Item> getItemsByUserWithFilters(User user, String search, UUID categoryId, 
                                               Item.ItemStatus status, Pageable pageable) {
        return itemRepository.findByUserWithFilters(user, search, categoryId, status, pageable);
    }
    
    public Optional<Item> getItemById(UUID id) {
        return itemRepository.findById(id);
    }
    
    public Item updateItem(UUID id, String name, BigDecimal purchasePrice, LocalDate purchaseDate,
                          String brand, String size, String color, String location, String images) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("物品不存在"));
        
        item.setName(name);
        item.setPurchasePrice(purchasePrice);
        item.setPurchaseDate(purchaseDate);
        item.setBrand(brand);
        item.setSize(size);
        item.setColor(color);
        item.setLocation(location);
        item.setImages(images);
        
        return itemRepository.save(item);
    }
    
    public void deleteItem(UUID id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("物品不存在"));
        
        item.setDeletedAt(LocalDateTime.now());
        itemRepository.save(item);
    }
    
    public List<Item> getItemsByUserAndStatus(User user, Item.ItemStatus status) {
        return itemRepository.findByUserAndStatusAndDeletedAtIsNull(user, status);
    }
    
    public List<Item> getItemsByUserAndDateRange(User user, LocalDate startDate, LocalDate endDate) {
        return itemRepository.findByUserAndPurchaseDateBetween(user, startDate, endDate);
    }
    
    public Double getTotalValueByUser(User user) {
        Double total = itemRepository.getTotalValueByUser(user);
        return total != null ? total : 0.0;
    }
    
    public List<Object[]> getValueByCategory(User user) {
        return itemRepository.getValueByCategory(user);
    }
}
