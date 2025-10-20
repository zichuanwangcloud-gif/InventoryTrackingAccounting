package app.inv.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Data
@EqualsAndHashCode(callSuper = false)
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // 关联关系
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LedgerEntry> ledgerEntries = new ArrayList<>();
    
    public enum AccountType {
        CASH, BANK, PLATFORM, OTHER
    }
}
