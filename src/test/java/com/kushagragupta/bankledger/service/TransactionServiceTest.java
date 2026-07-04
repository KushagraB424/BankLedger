package com.kushagragupta.bankledger.service;

import com.kushagragupta.bankledger.entity.Account;
import com.kushagragupta.bankledger.entity.AccountStatus;
import com.kushagragupta.bankledger.entity.Transaction;
import com.kushagragupta.bankledger.entity.TransactionStatus;
import com.kushagragupta.bankledger.entity.TransactionType;
import com.kushagragupta.bankledger.exception.InvalidTransactionException;
import com.kushagragupta.bankledger.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TransactionService transactionService;

    private Account sourceAccount;
    private Account destinationAccount;
    private UUID sourceId;
    private UUID destId;

    @BeforeEach
    void setUp() {
        sourceId = UUID.randomUUID();
        destId = UUID.randomUUID();
        
        sourceAccount = Account.builder()
                .id(sourceId)
                .balance(new BigDecimal("1000.00"))
                .status(AccountStatus.ACTIVE)
                .build();
                
        destinationAccount = Account.builder()
                .id(destId)
                .balance(new BigDecimal("500.00"))
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    void deposit_Success() {
        BigDecimal amount = new BigDecimal("500.00");
        when(accountService.getAccountById(sourceId)).thenReturn(sourceAccount);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        Transaction result = transactionService.deposit(sourceId, amount, "Test Deposit");

        assertNotNull(result);
        assertEquals(TransactionType.DEPOSIT, result.getType());
        assertEquals(amount, result.getAmount());
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        verify(accountService).creditAccount(sourceId, amount);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void withdraw_Success() {
        BigDecimal amount = new BigDecimal("200.00");
        when(accountService.getAccountById(sourceId)).thenReturn(sourceAccount);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        Transaction result = transactionService.withdraw(sourceId, amount, "Test Withdraw");

        assertNotNull(result);
        assertEquals(TransactionType.WITHDRAWAL, result.getType());
        assertEquals(amount, result.getAmount());
        verify(accountService).debitAccount(sourceId, amount);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transfer_Success() {
        BigDecimal amount = new BigDecimal("300.00");
        when(accountService.getAccountById(sourceId)).thenReturn(sourceAccount);
        when(accountService.getAccountById(destId)).thenReturn(destinationAccount);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        Transaction result = transactionService.transfer(sourceId, destId, amount, "Test Transfer");

        assertNotNull(result);
        assertEquals(TransactionType.TRANSFER, result.getType());
        assertEquals(amount, result.getAmount());
        verify(accountService).debitAccount(sourceId, amount);
        verify(accountService).creditAccount(destId, amount);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transfer_ToSameAccount_ThrowsException() {
        assertThrows(InvalidTransactionException.class, 
                () -> transactionService.transfer(sourceId, sourceId, new BigDecimal("100.00"), "Test"));
        
        verify(accountService, never()).debitAccount(any(), any());
        verify(accountService, never()).creditAccount(any(), any());
        verify(transactionRepository, never()).save(any());
    }
}
