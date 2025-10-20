package app.inv.repository;

import app.inv.entity.LedgerEntry;
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
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    
    Page<LedgerEntry> findByUserOrderByTransactionDateDesc(User user, Pageable pageable);
    
    @Query("SELECT l FROM LedgerEntry l WHERE l.user = :user " +
           "AND (:accountId IS NULL OR l.account.id = :accountId) " +
           "AND (:startDate IS NULL OR l.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR l.transactionDate <= :endDate)")
    Page<LedgerEntry> findByUserWithFilters(@Param("user") User user,
                                           @Param("accountId") UUID accountId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate,
                                           Pageable pageable);
    
    @Query("SELECT l.direction, SUM(l.amount) FROM LedgerEntry l " +
           "WHERE l.user = :user AND l.account.id = :accountId " +
           "AND l.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY l.direction")
    List<Object[]> getAccountBalanceByDateRange(@Param("user") User user,
                                                @Param("accountId") UUID accountId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);
    
    @Query("SELECT l.categoryCode, SUM(l.amount) FROM LedgerEntry l " +
           "WHERE l.user = :user AND l.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY l.categoryCode")
    List<Object[]> getAmountByCategoryAndDateRange(@Param("user") User user,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);
}
