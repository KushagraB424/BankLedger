package com.kushagragupta.bankledger.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {
    
    @NotNull(message = "Source account ID is required")
    private UUID sourceAccountId;

    @NotNull(message = "Destination account ID is required")
    private UUID destinationAccountId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be strictly positive")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @Digits(integer = 15, fraction = 2, message = "Amount cannot have more than 2 decimal places")
    private BigDecimal amount;
    
    private String description;
}
