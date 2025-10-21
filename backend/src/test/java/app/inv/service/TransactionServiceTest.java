package app.inv.service;

import app.inv.entity.*;
import app.inv.repository.InventoryTransactionRepository;
import app.inv.repository.LedgerEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private InventoryTransactionRepository transactionRepository;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Item testItem;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");

        testItem = new Item();
        testItem.setId(UUID.randomUUID());
        testItem.setName("测试物品");
        testItem.setUser(testUser);

        testAccount = new Account();
        testAccount.setId(UUID.randomUUID());
        testAccount.setName("测试账户");
        testAccount.setType(Account.AccountType.CASH);
        testAccount.setUser(testUser);
    }

    @Test
    void createTransaction_shouldCreateTransactionAndLedgerEntries() {
        // Given
        Integer quantity = 10;
        BigDecimal unitPrice = new BigDecimal("100.00");
        LocalDate transactionDate = LocalDate.now();
        InventoryTransaction.TransactionType type = InventoryTransaction.TransactionType.IN;
        InventoryTransaction.TransactionReason reason = InventoryTransaction.TransactionReason.PURCHASE;
        String notes = "测试交易";

        InventoryTransaction savedTransaction = new InventoryTransaction();
        savedTransaction.setId(UUID.randomUUID());
        savedTransaction.setUser(testUser);
        savedTransaction.setItem(testItem);
        savedTransaction.setType(type);
        savedTransaction.setQuantity(quantity);
        savedTransaction.setUnitPrice(unitPrice);
        savedTransaction.setTotalAmount(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        savedTransaction.setTransactionDate(transactionDate);
        savedTransaction.setReason(reason);
        savedTransaction.setNotes(notes);

        when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(savedTransaction);
        when(ledgerEntryRepository.save(any(LedgerEntry.class))).thenReturn(new LedgerEntry());

        // When
        InventoryTransaction result = transactionService.createTransaction(
                testUser, testItem, type, quantity, unitPrice, 
                transactionDate, reason, notes, testAccount);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getItem()).isEqualTo(testItem);
        assertThat(result.getType()).isEqualTo(type);
        assertThat(result.getQuantity()).isEqualTo(quantity);
        assertThat(result.getUnitPrice()).isEqualTo(unitPrice);
        assertThat(result.getTotalAmount()).isEqualTo(unitPrice.multiply(BigDecimal.valueOf(quantity)));

        verify(transactionRepository).save(any(InventoryTransaction.class));
        verify(ledgerEntryRepository, atLeast(1)).save(any(LedgerEntry.class));
    }

    @Test
    void createTransaction_shouldCalculateTotalAmountCorrectly() {
        // Given
        Integer quantity = 5;
        BigDecimal unitPrice = new BigDecimal("50.00");
        BigDecimal expectedTotal = new BigDecimal("250.00");

        when(transactionRepository.save(any(InventoryTransaction.class))).thenAnswer(invocation -> {
            InventoryTransaction transaction = invocation.getArgument(0);
            transaction.setId(UUID.randomUUID());
            return transaction;
        });
        when(ledgerEntryRepository.save(any(LedgerEntry.class))).thenReturn(new LedgerEntry());

        // When
        InventoryTransaction result = transactionService.createTransaction(
                testUser, testItem, InventoryTransaction.TransactionType.IN, 
                quantity, unitPrice, LocalDate.now(), 
                InventoryTransaction.TransactionReason.PURCHASE, "测试", testAccount);

        // Then
        assertThat(result.getTotalAmount()).isEqualTo(expectedTotal);
    }
}
