package com.kushagragupta.bankledger.service;

import com.kushagragupta.bankledger.entity.Account;
import com.kushagragupta.bankledger.entity.AccountStatus;
import com.kushagragupta.bankledger.entity.AccountType;
import com.kushagragupta.bankledger.entity.Customer;
import com.kushagragupta.bankledger.exception.InsufficientBalanceException;
import com.kushagragupta.bankledger.exception.InvalidTransactionException;
import com.kushagragupta.bankledger.exception.ResourceNotFoundException;
import com.kushagragupta.bankledger.repository.AccountRepository;
import com.kushagragupta.bankledger.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public Account createAccount(UUID customerId, AccountType type) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Account account = Account.builder()
                .accountNumber(generateUniqueAccountNumber())
                .accountType(type)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .customer(customer)
                .build();

        Account savedAccount = accountRepository.save(account);
        log.info("Created new account {} for customer {}", savedAccount.getAccountNumber(), customerId);
        return savedAccount;
    }

    public Account getAccountById(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    public List<Account> getAccountsByCustomerId(UUID customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    public void creditAccount(UUID accountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Credit amount must be greater than zero");
        }
        if (amount.scale() > 2) {
            throw new InvalidTransactionException("Amount cannot have more than 2 decimal places");
        }

        Account account = getAccountById(accountId);
        
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTransactionException("Account is not active");
        }

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }

    public void debitAccount(UUID accountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Debit amount must be greater than zero");
        }
        if (amount.scale() > 2) {
            throw new InvalidTransactionException("Amount cannot have more than 2 decimal places");
        }

        Account account = getAccountById(accountId);
        
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTransactionException("Account is not active");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient funds for withdrawal");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            long number = ThreadLocalRandom.current().nextLong(1000000000L, 10000000000L);
            accountNumber = String.valueOf(number);
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}
