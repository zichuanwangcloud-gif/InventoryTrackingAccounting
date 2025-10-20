package app.inv.service;

import app.inv.entity.*;
import app.inv.repository.InventoryTransactionRepository;
import app.inv.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {
    
    private final InventoryTransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    
    public InventoryTransaction createTransaction(User user, Item item, 
                                                InventoryTransaction.TransactionType type,
                                                Integer quantity, BigDecimal unitPrice,
                                                LocalDate transactionDate,
                                                InventoryTransaction.TransactionReason reason,
                                                String notes, Account account) {
        
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setUser(user);
        transaction.setItem(item);
        transaction.setType(type);
        transaction.setQuantity(quantity);
        transaction.setUnitPrice(unitPrice);
        transaction.setTotalAmount(totalAmount);
        transaction.setTransactionDate(transactionDate);
        transaction.setReason(reason);
        transaction.setNotes(notes);
        
        InventoryTransaction savedTransaction = transactionRepository.save(transaction);
        
        // 生成记账分录
        generateLedgerEntries(user, item, type, totalAmount, transactionDate, account, reason);
        
        // 更新物品状态
        if (type == InventoryTransaction.TransactionType.OUT) {
            item.setStatus(Item.ItemStatus.REMOVED);
        }
        
        return savedTransaction;
    }
    
    public Page<InventoryTransaction> getTransactionsByUser(User user, Pageable pageable) {
        return transactionRepository.findByUserOrderByTransactionDateDesc(user, pageable);
    }
    
    public Page<InventoryTransaction> getTransactionsByUserWithFilters(User user,
                                                                     InventoryTransaction.TransactionType type,
                                                                     UUID itemId, LocalDate startDate, LocalDate endDate,
                                                                     Pageable pageable) {
        return transactionRepository.findByUserWithFilters(user, type, itemId, startDate, endDate, pageable);
    }
    
    public List<InventoryTransaction> getTransactionsByUserAndTypeAndDateRange(
            User user, InventoryTransaction.TransactionType type, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByUserAndTypeAndTransactionDateBetween(user, type, startDate, endDate);
    }
    
    public Double getTotalAmountByUserAndTypeAndDateRange(User user, InventoryTransaction.TransactionType type,
                                                         LocalDate startDate, LocalDate endDate) {
        Double total = transactionRepository.getTotalAmountByUserAndTypeAndDateRange(user, type, startDate, endDate);
        return total != null ? total : 0.0;
    }
    
    public List<Object[]> getOutboundAmountByReason(User user, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.getOutboundAmountByReason(user, startDate, endDate);
    }
    
    private void generateLedgerEntries(User user, Item item, InventoryTransaction.TransactionType type,
                                     BigDecimal amount, LocalDate transactionDate, Account account,
                                     InventoryTransaction.TransactionReason reason) {
        
        if (type == InventoryTransaction.TransactionType.IN) {
            // 入库：借：存货，贷：现金/银行
            createLedgerEntry(user, transactionDate, amount, LedgerEntry.Direction.DEBIT, 
                           account, item, "INVENTORY", "物品入库");
            createLedgerEntry(user, transactionDate, amount, LedgerEntry.Direction.CREDIT, 
                           account, item, "CASH", "物品入库");
        } else if (type == InventoryTransaction.TransactionType.OUT) {
            if (reason == InventoryTransaction.TransactionReason.SELL) {
                // 转售：借：现金，贷：存货+收益
                createLedgerEntry(user, transactionDate, amount, LedgerEntry.Direction.DEBIT, 
                               account, item, "CASH", "物品转售");
                createLedgerEntry(user, transactionDate, amount, LedgerEntry.Direction.CREDIT, 
                               account, item, "INVENTORY", "物品转售");
            } else {
                // 丢弃/赠与：借：损失，贷：存货
                createLedgerEntry(user, transactionDate, amount, LedgerEntry.Direction.DEBIT, 
                               account, item, "LOSS", "物品处置");
                createLedgerEntry(user, transactionDate, amount, LedgerEntry.Direction.CREDIT, 
                               account, item, "INVENTORY", "物品处置");
            }
        }
    }
    
    private void createLedgerEntry(User user, LocalDate transactionDate, BigDecimal amount,
                                  LedgerEntry.Direction direction, Account account, Item item,
                                  String categoryCode, String note) {
        LedgerEntry entry = new LedgerEntry();
        entry.setUser(user);
        entry.setTransactionDate(transactionDate);
        entry.setAmount(amount);
        entry.setDirection(direction);
        entry.setAccount(account);
        entry.setItem(item);
        entry.setCategoryCode(categoryCode);
        entry.setNote(note);
        
        ledgerEntryRepository.save(entry);
    }
}
