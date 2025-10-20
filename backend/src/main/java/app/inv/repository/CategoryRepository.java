package app.inv.repository;

import app.inv.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    
    List<Category> findByParentIsNullOrderByName();
    
    List<Category> findByParentIdOrderByName(UUID parentId);
    
    @Query("SELECT c FROM Category c WHERE c.name LIKE %:name%")
    List<Category> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL")
    List<Category> findRootCategories();
    
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId")
    List<Category> findByParentId(@Param("parentId") UUID parentId);
}
