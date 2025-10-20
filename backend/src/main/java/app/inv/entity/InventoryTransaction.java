package app.inv.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory_transactions")
@Data
@EqualsAndHashCode(callSuper = false)
public class InventoryTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;
    
    @Column(nullable = false)
    private Integer quantity = 1;
    
    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;
    
    @Enumerated(EnumType.STRING)
    private TransactionReason reason;
    
    @Column(columnDefinition = "text")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public enum TransactionType {
        IN, OUT, ADJUST
    }
    
    public enum TransactionReason {
        PURCHASE, SELL, DISPOSE, GIFT, LOST, ADJUST
    }
}
