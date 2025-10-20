package app.inv.repository;

import app.inv.entity.InventoryTransaction;
import app.inv.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, UUID> {
    
    Page<InventoryTransaction> findByUserOrderByTransactionDateDesc(User user, Pageable pageable);
    
    @Query("SELECT t FROM InventoryTransaction t WHERE t.user = :user " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:itemId IS NULL OR t.item.id = :itemId) " +
           "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.transactionDate <= :endDate)")
    Page<InventoryTransaction> findByUserWithFilters(@Param("user") User user,
                                                   @Param("type") InventoryTransaction.TransactionType type,
                                                   @Param("itemId") UUID itemId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate,
                                                   Pageable pageable);
    
    List<InventoryTransaction> findByUserAndTypeAndTransactionDateBetween(
            User user, InventoryTransaction.TransactionType type, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT SUM(t.totalAmount) FROM InventoryTransaction t " +
           "WHERE t.user = :user AND t.type = :type " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate")
    Double getTotalAmountByUserAndTypeAndDateRange(@Param("user") User user,
                                                   @Param("type") InventoryTransaction.TransactionType type,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
    
    @Query("SELECT t.reason, SUM(t.totalAmount) FROM InventoryTransaction t " +
           "WHERE t.user = :user AND t.type = 'OUT' " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY t.reason")
    List<Object[]> getOutboundAmountByReason(@Param("user") User user,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);
}
