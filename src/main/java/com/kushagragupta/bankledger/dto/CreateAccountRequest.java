package com.kushagragupta.bankledger.dto;

import com.kushagragupta.bankledger.entity.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountRequest {
    @NotNull(message = "Account type is required")
    private AccountType accountType;
}
