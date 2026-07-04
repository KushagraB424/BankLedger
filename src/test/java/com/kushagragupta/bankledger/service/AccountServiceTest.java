package com.kushagragupta.bankledger.service;

import com.kushagragupta.bankledger.entity.Account;
import com.kushagragupta.bankledger.entity.AccountStatus;
import com.kushagragupta.bankledger.entity.AccountType;
import com.kushagragupta.bankledger.entity.Customer;
import com.kushagragupta.bankledger.exception.ResourceNotFoundException;
import com.kushagragupta.bankledger.repository.AccountRepository;
import com.kushagragupta.bankledger.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountService accountService;

    private Customer customer;
    private Account account;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .build();

        account = Account.builder()
                .id(UUID.randomUUID())
                .accountNumber("1234567890")
                .accountType(AccountType.CURRENT)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .customer(customer)
                .build();
    }

    @Test
    void createAccount_Success() {
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArguments()[0]);

        Account result = accountService.createAccount(customer.getId(), AccountType.CURRENT);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertEquals(AccountType.CURRENT, result.getAccountType());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getAccountNumber());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_CustomerNotFound_ThrowsException() {
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.createAccount(UUID.randomUUID(), AccountType.SAVINGS));
    }

    @Test
    void getAccountsByCustomerId_Success() {
        when(accountRepository.findByCustomerId(customer.getId())).thenReturn(List.of(account));

        List<Account> accounts = accountService.getAccountsByCustomerId(customer.getId());

        assertFalse(accounts.isEmpty());
        assertEquals(1, accounts.size());
        assertEquals(account.getAccountNumber(), accounts.get(0).getAccountNumber());
    }

    @Test
    void getAccountById_Success() {
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        Account result = accountService.getAccountById(account.getId());

        assertNotNull(result);
        assertEquals(account.getId(), result.getId());
    }
}
