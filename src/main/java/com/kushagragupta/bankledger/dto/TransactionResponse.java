package com.kushagragupta.bankledger.dto;

import com.kushagragupta.bankledger.entity.TransactionStatus;
import com.kushagragupta.bankledger.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private UUID id;
    private String transactionReference;
    private TransactionType type;
    private BigDecimal amount;
    private Instant timestamp;
    private String description;
    private TransactionStatus status;
}
