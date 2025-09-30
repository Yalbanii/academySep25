package com.xideral.banco.account.dto;

import com.xideral.banco.account.model.Account;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Account type is required")
    private Account.AccountType accountType;

    @DecimalMin(value = "0.0", inclusive = true, message = "Initial balance must be zero or positive")
    private BigDecimal initialBalance = BigDecimal.ZERO;
}