package com.kushagragupta.bankledger.controller;

import com.kushagragupta.bankledger.dto.TransactionResponse;
import com.kushagragupta.bankledger.dto.TransferRequest;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "Endpoints for money transfers")
public class TransferController {

    private final TransactionService transactionService;
    private final AccountService accountService;

    @Operation(summary = "Transfer money", description = "Transfers money from a source account to a destination account")
    @PostMapping
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        verifyOwnership(request.getSourceAccountId(), userDetails.getCustomer());
        Transaction transaction = transactionService.transfer(
                request.getSourceAccountId(),
                request.getDestinationAccountId(),
                request.getAmount(),
                request.getDescription()
        );
        return ResponseEntity.ok(mapToResponse(transaction));
    }

    private void verifyOwnership(UUID accountId, Customer customer) {
        Account account = accountService.getAccountById(accountId);
        if (!account.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to transfer from this account");
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
