package app.inv.service;

import app.inv.entity.*;
import app.inv.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {
    
    private final ItemService itemService;
    private final TransactionService transactionService;
    private final LedgerEntryRepository ledgerEntryRepository;
    
    public Map<String, Object> getInventoryValueReport(User user) {
        Map<String, Object> report = new HashMap<>();
        
        // 总库存价值
        Double totalValue = itemService.getTotalValueByUser(user);
        report.put("totalValue", totalValue);
        
        // 按品类分组
        List<Object[]> categoryValues = itemService.getValueByCategory(user);
        Map<String, Double> categoryMap = new HashMap<>();
        for (Object[] row : categoryValues) {
            String categoryName = (String) row[0];
            Double value = ((BigDecimal) row[1]).doubleValue();
            categoryMap.put(categoryName, value);
        }
        report.put("categoryValues", categoryMap);
        
        return report;
    }
    
    public Map<String, Object> getDisposalProfitReport(User user, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        
        // 转售收益
        List<Object[]> outboundAmounts = transactionService.getOutboundAmountByReason(user, startDate, endDate);
        Map<String, Double> reasonMap = new HashMap<>();
        for (Object[] row : outboundAmounts) {
            String reason = (String) row[0];
            Double amount = ((BigDecimal) row[1]).doubleValue();
            reasonMap.put(reason, amount);
        }
        report.put("disposalAmounts", reasonMap);
        
        return report;
    }
    
    public Map<String, Object> getTrendsReport(User user, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        
        // 入库金额
        Double inboundAmount = transactionService.getTotalAmountByUserAndTypeAndDateRange(
                user, InventoryTransaction.TransactionType.IN, startDate, endDate);
        report.put("inboundAmount", inboundAmount);
        
        // 出库金额
        Double outboundAmount = transactionService.getTotalAmountByUserAndTypeAndDateRange(
                user, InventoryTransaction.TransactionType.OUT, startDate, endDate);
        report.put("outboundAmount", outboundAmount);
        
        // 净额
        Double netAmount = inboundAmount - outboundAmount;
        report.put("netAmount", netAmount);
        
        return report;
    }
    
    public Page<LedgerEntry> getLedgerEntries(User user, UUID accountId, LocalDate startDate, 
                                           LocalDate endDate, Pageable pageable) {
        return ledgerEntryRepository.findByUserWithFilters(user, accountId, startDate, endDate, pageable);
    }
    
    public Map<String, Object> getAccountBalance(User user, UUID accountId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> balance = new HashMap<>();
        
        List<Object[]> balanceData = ledgerEntryRepository.getAccountBalanceByDateRange(user, accountId, startDate, endDate);
        
        Double debitTotal = 0.0;
        Double creditTotal = 0.0;
        
        for (Object[] row : balanceData) {
            String direction = (String) row[0];
            Double amount = ((BigDecimal) row[1]).doubleValue();
            
            if ("DEBIT".equals(direction)) {
                debitTotal += amount;
            } else if ("CREDIT".equals(direction)) {
                creditTotal += amount;
            }
        }
        
        balance.put("debitTotal", debitTotal);
        balance.put("creditTotal", creditTotal);
        balance.put("balance", debitTotal - creditTotal);
        
        return balance;
    }
    
    public List<Object[]> getAmountByCategory(User user, LocalDate startDate, LocalDate endDate) {
        return ledgerEntryRepository.getAmountByCategoryAndDateRange(user, startDate, endDate);
    }
}
