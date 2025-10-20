package app.inv.service;

import app.inv.entity.Account;
import app.inv.entity.User;
import app.inv.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {
    
    private final AccountRepository accountRepository;
    
    public Account createAccount(User user, String name, Account.AccountType type) {
        Account account = new Account();
        account.setUser(user);
        account.setName(name);
        account.setType(type);
        
        return accountRepository.save(account);
    }
    
    public List<Account> getAccountsByUser(User user) {
        return accountRepository.findByUserOrderByName(user);
    }
    
    public List<Account> getAccountsByUserAndType(User user, Account.AccountType type) {
        return accountRepository.findByUserAndType(user, type);
    }
    
    public Optional<Account> getAccountById(UUID id) {
        return accountRepository.findById(id);
    }
    
    public Account updateAccount(UUID id, String name, Account.AccountType type) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("账户不存在"));
        
        account.setName(name);
        account.setType(type);
        
        return accountRepository.save(account);
    }
    
    public void deleteAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("账户不存在"));
        
        accountRepository.delete(account);
    }
    
    public Account getAccountByUserAndName(User user, String name) {
        return accountRepository.findByUserAndName(user, name);
    }
    
    public Account getAccountByUserAndId(User user, UUID id) {
        return accountRepository.findByUserAndId(user, id);
    }
}
