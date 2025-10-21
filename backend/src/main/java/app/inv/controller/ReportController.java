package app.inv.controller;

import app.inv.dto.ApiResponse;
import app.inv.entity.User;
import app.inv.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {
    
    private final ReportService reportService;
    
    @GetMapping("/inventory-value")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInventoryValueReport(
            @RequestParam(required = false) String groupBy) {
        
        // TODO: 从JWT Token中获取当前用户
        User currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        
        Map<String, Object> report = reportService.getInventoryValueReport(currentUser);
        
        return ResponseEntity.ok(ApiResponse.success(report));
    }
    
    @GetMapping("/disposal-profit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDisposalProfitReport(
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
        
        Map<String, Object> report = reportService.getDisposalProfitReport(currentUser, startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(report));
    }
    
    @GetMapping("/trends")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTrendsReport(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        
        // TODO: 从JWT Token中获取当前用户
        User currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(6);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        Map<String, Object> report = reportService.getTrendsReport(currentUser, startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(report));
    }
    
    @GetMapping("/ledger")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLedgerEntries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        
        // TODO: 从JWT Token中获取当前用户
        User currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        
        Pageable pageable = PageRequest.of(page, size);
        
        // TODO: 实现分录查询
        Map<String, Object> result = new HashMap<>();
        result.put("content", new ArrayList<>());
        result.put("totalElements", 0);
        result.put("totalPages", 0);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
