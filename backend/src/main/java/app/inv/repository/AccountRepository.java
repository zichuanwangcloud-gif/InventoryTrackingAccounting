package app.inv.repository;

import app.inv.entity.Account;
import app.inv.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    
    List<Account> findByUserOrderByName(User user);
    
    List<Account> findByUserAndType(User user, Account.AccountType type);
    
    @Query("SELECT a FROM Account a WHERE a.user = :user AND a.name = :name")
    Account findByUserAndName(@Param("user") User user, @Param("name") String name);
    
    @Query("SELECT a FROM Account a WHERE a.user = :user AND a.id = :id")
    Account findByUserAndId(@Param("user") User user, @Param("id") UUID id);
}
