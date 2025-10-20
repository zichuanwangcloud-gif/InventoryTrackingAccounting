package app.inv.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "items")
@Data
@EqualsAndHashCode(callSuper = false)
public class Item {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Column(length = 100)
    private String brand;
    
    @Column(length = 50)
    private String size;
    
    @Column(length = 50)
    private String color;
    
    @Column(name = "purchase_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal purchasePrice;
    
    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;
    
    @Column(length = 200)
    private String location;
    
    @Column(columnDefinition = "jsonb")
    private String images; // JSON字符串存储图片URL数组
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status = ItemStatus.ACTIVE;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // 软删除
    
    // 关联关系
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InventoryTransaction> transactions = new ArrayList<>();
    
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LedgerEntry> ledgerEntries = new ArrayList<>();
    
    public enum ItemStatus {
        ACTIVE, REMOVED
    }
}
