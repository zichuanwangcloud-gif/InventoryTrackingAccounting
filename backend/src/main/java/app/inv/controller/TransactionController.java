package app.inv.controller;

import app.inv.dto.ApiResponse;
import app.inv.entity.InventoryTransaction;
import app.inv.entity.User;
import app.inv.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    
    private final TransactionService transactionService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<InventoryTransaction>>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) InventoryTransaction.TransactionType type,
            @RequestParam(required = false) UUID itemId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        
        // TODO: 从JWT Token中获取当前用户
        User currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<InventoryTransaction> transactions = transactionService.getTransactionsByUserWithFilters(
                currentUser, type, itemId, startDate, endDate, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryTransaction>> getTransaction(@PathVariable UUID id) {
        // TODO: 实现获取单个交易详情
        return ResponseEntity.ok(ApiResponse.success("交易详情获取成功", null));
    }
    
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTransactionStats(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        
        // TODO: 从JWT Token中获取当前用户
        User currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("inboundAmount", transactionService.getTotalAmountByUserAndTypeAndDateRange(
                currentUser, InventoryTransaction.TransactionType.IN, startDate, endDate));
        stats.put("outboundAmount", transactionService.getTotalAmountByUserAndTypeAndDateRange(
                currentUser, InventoryTransaction.TransactionType.OUT, startDate, endDate));
        stats.put("disposalAmounts", transactionService.getOutboundAmountByReason(currentUser, startDate, endDate));
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
