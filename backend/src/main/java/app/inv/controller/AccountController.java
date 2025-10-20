package app.inv.controller;

import app.inv.dto.ApiResponse;
import app.inv.entity.Account;
import app.inv.entity.User;
import app.inv.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {
    
    private final AccountService accountService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Account>>> getAccounts() {
        // TODO: 从JWT Token中获取当前用户
        User currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        
        List<Account> accounts = accountService.getAccountsByUser(currentUser);
        
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Account>> getAccount(@PathVariable UUID id) {
        Account account = accountService.getAccountById(id)
                .orElseThrow(() -> new RuntimeException("账户不存在"));
        
        return ResponseEntity.ok(ApiResponse.success(account));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<Account>> createAccount(@RequestBody Map<String, Object> request) {
        try {
            // TODO: 从JWT Token中获取当前用户
            User currentUser = new User();
            currentUser.setId(UUID.randomUUID());
            
            String name = (String) request.get("name");
            String typeStr = (String) request.get("type");
            Account.AccountType type = Account.AccountType.valueOf(typeStr);
            
            Account account = accountService.createAccount(currentUser, name, type);
            
            return ResponseEntity.ok(ApiResponse.success("账户创建成功", account));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Account>> updateAccount(@PathVariable UUID id, 
                                                           @RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String typeStr = (String) request.get("type");
            Account.AccountType type = Account.AccountType.valueOf(typeStr);
            
            Account account = accountService.updateAccount(id, name, type);
            
            return ResponseEntity.ok(ApiResponse.success("账户更新成功", account));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable UUID id) {
        try {
            accountService.deleteAccount(id);
            return ResponseEntity.ok(ApiResponse.success("账户删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
