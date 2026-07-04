package com.kushagragupta.bankledger.controller;

import com.kushagragupta.bankledger.dto.DepositRequest;
import com.kushagragupta.bankledger.dto.TransactionResponse;
import com.kushagragupta.bankledger.dto.WithdrawRequest;
import com.kushagragupta.bankledger.entity.Account;
import com.kushagragupta.bankledger.entity.Customer;
import com.kushagragupta.bankledger.entity.Transaction;
import com.kushagragupta.bankledger.exception.UnauthorizedAccessException;
import com.kushagragupta.bankledger.security.CustomUserDetails;
import com.kushagragupta.bankledger.service.AccountService;
import com.kushagragupta.bankledger.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v1/accounts/{accountId}")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Endpoints for deposits, withdrawals, and history")
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountService accountService;

    @Operation(summary = "Deposit money", description = "Deposits money into the specified account")
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable UUID accountId,
            @Valid @RequestBody DepositRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        verifyOwnership(accountId, userDetails.getCustomer());
        Transaction transaction = transactionService.deposit(accountId, request.getAmount(), request.getDescription());
        return ResponseEntity.ok(mapToResponse(transaction));
    }

    @Operation(summary = "Withdraw money", description = "Withdraws money from the specified account")
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable UUID accountId,
            @Valid @RequestBody WithdrawRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        verifyOwnership(accountId, userDetails.getCustomer());
        Transaction transaction = transactionService.withdraw(accountId, request.getAmount(), request.getDescription());
        return ResponseEntity.ok(mapToResponse(transaction));
    }

    @Operation(summary = "Get transaction history", description = "Fetches transaction history for the specified account")
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        verifyOwnership(accountId, userDetails.getCustomer());
        List<Transaction> transactions = transactionService.getTransactionHistory(accountId);
        List<TransactionResponse> responses = transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private void verifyOwnership(UUID accountId, Customer customer) {
        Account account = accountService.getAccountById(accountId);
        if (!account.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to access this account");
        }
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionReference(transaction.getTransactionReference())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .timestamp(transaction.getTimestamp())
                .description(transaction.getDescription())
                .status(transaction.getStatus())
                .build();
    }
}
