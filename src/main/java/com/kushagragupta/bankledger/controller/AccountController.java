package com.kushagragupta.bankledger.controller;

import com.kushagragupta.bankledger.dto.AccountResponse;
import com.kushagragupta.bankledger.dto.CreateAccountRequest;
import com.kushagragupta.bankledger.entity.Account;
import com.kushagragupta.bankledger.entity.Customer;
import com.kushagragupta.bankledger.exception.UnauthorizedAccessException;
import com.kushagragupta.bankledger.security.CustomUserDetails;
import com.kushagragupta.bankledger.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Endpoints for managing bank accounts")
public class AccountController {

    private final AccountService accountService;

    @Operation(
        summary = "Create a new account",
        description = "Creates a new account for the currently authenticated customer"
    )
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Customer customer = userDetails.getCustomer();
        Account created = accountService.createAccount(customer.getId(), request.getAccountType());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(created));
    }

    @Operation(
        summary = "Get account by ID",
        description = "Fetches details of a specific account owned by the authenticated customer"
    )
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Account account = accountService.getAccountById(id);
        verifyOwnership(account, userDetails.getCustomer());
        return ResponseEntity.ok(mapToResponse(account));
    }

    @Operation(
        summary = "Get all accounts",
        description = "Fetches all accounts owned by the authenticated customer"
    )
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Customer customer = userDetails.getCustomer();
        List<Account> accounts = accountService.getAccountsByCustomerId(customer.getId());
        List<AccountResponse> responses = accounts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private void verifyOwnership(Account account, Customer customer) {
        if (!account.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to access this account");
        }
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
