package com.kushagragupta.bankledger.service;

import com.kushagragupta.bankledger.entity.Account;
import com.kushagragupta.bankledger.entity.Transaction;
import com.kushagragupta.bankledger.entity.TransactionStatus;
import com.kushagragupta.bankledger.entity.TransactionType;
import com.kushagragupta.bankledger.exception.InvalidTransactionException;
import com.kushagragupta.bankledger.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    @Transactional
    public Transaction deposit(UUID accountId, BigDecimal amount, String description) {
        accountService.creditAccount(accountId, amount);
        Account account = accountService.getAccountById(accountId);

        Transaction transaction = Transaction.builder()
                .transactionReference(generateTransactionReference())
                .type(TransactionType.DEPOSIT)
                .amount(amount)
                .description(description)
                .sourceAccount(account)
                .status(TransactionStatus.COMPLETED)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Deposit successful: {} to account {}", amount, accountId);
        return saved;
    }

    @Transactional
    public Transaction withdraw(UUID accountId, BigDecimal amount, String description) {
        accountService.debitAccount(accountId, amount);
        Account account = accountService.getAccountById(accountId);

        Transaction transaction = Transaction.builder()
                .transactionReference(generateTransactionReference())
                .type(TransactionType.WITHDRAWAL)
                .amount(amount)
                .description(description)
                .sourceAccount(account)
                .status(TransactionStatus.COMPLETED)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Withdrawal successful: {} from account {}", amount, accountId);
        return saved;
    }

    @Transactional
    public Transaction transfer(UUID sourceAccountId, UUID destinationAccountId, BigDecimal amount, String description) {
        if (sourceAccountId.equals(destinationAccountId)) {
            throw new InvalidTransactionException("Cannot transfer to the same account");
        }

        // Debit source account first
        accountService.debitAccount(sourceAccountId, amount);
        Account sourceAccount = accountService.getAccountById(sourceAccountId);

        // Credit destination account
        accountService.creditAccount(destinationAccountId, amount);
        Account destinationAccount = accountService.getAccountById(destinationAccountId);

        Transaction transaction = Transaction.builder()
                .transactionReference(generateTransactionReference())
                .type(TransactionType.TRANSFER)
                .amount(amount)
                .description(description)
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .status(TransactionStatus.COMPLETED)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transfer successful: {} from account {} to account {}", amount, sourceAccountId, destinationAccountId);
        return saved;
    }

    public List<Transaction> getTransactionHistory(UUID accountId) {
        // Just verify account exists first
        accountService.getAccountById(accountId);
        return transactionRepository.findBySourceAccountIdOrDestinationAccountIdOrderByTimestampDesc(accountId, accountId);
    }

    private String generateTransactionReference() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder("TXN-");
        
        // Example: TXN-20260705-8F3A91C2, but let's stick to TXN-AB12CD34EF style short alphanumeric.
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        return sb.toString();
    }
}
