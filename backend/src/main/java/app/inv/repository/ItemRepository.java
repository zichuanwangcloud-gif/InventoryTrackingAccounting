package app.inv.repository;

import app.inv.entity.Item;
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
public interface ItemRepository extends JpaRepository<Item, UUID> {
    
    Page<Item> findByUserAndDeletedAtIsNull(User user, Pageable pageable);
    
    @Query("SELECT i FROM Item i WHERE i.user = :user AND i.deletedAt IS NULL " +
           "AND (:search IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(i.brand) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:categoryId IS NULL OR i.category.id = :categoryId) " +
           "AND (:status IS NULL OR i.status = :status)")
    Page<Item> findByUserWithFilters(@Param("user") User user,
                                    @Param("search") String search,
                                    @Param("categoryId") UUID categoryId,
                                    @Param("status") Item.ItemStatus status,
                                    Pageable pageable);
    
    List<Item> findByUserAndStatusAndDeletedAtIsNull(User user, Item.ItemStatus status);
    
    @Query("SELECT i FROM Item i WHERE i.user = :user AND i.deletedAt IS NULL " +
           "AND i.purchaseDate BETWEEN :startDate AND :endDate")
    List<Item> findByUserAndPurchaseDateBetween(@Param("user") User user,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(i.purchasePrice) FROM Item i WHERE i.user = :user " +
           "AND i.status = 'ACTIVE' AND i.deletedAt IS NULL")
    Double getTotalValueByUser(@Param("user") User user);
    
    @Query("SELECT i.category.name, SUM(i.purchasePrice) FROM Item i " +
           "WHERE i.user = :user AND i.status = 'ACTIVE' AND i.deletedAt IS NULL " +
           "GROUP BY i.category.name")
    List<Object[]> getValueByCategory(@Param("user") User user);
}
