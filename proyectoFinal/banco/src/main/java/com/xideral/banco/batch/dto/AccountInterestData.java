package com.xideral.banco.batch.dto;

import com.xideral.banco.account.model.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO que contiene la información de una cuenta y su interés calculado.
 * Se usa para pasar datos entre los steps del batch job.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountInterestData {

    private Long accountId;
    private String accountNumber;
    private Account.AccountType accountType;
    private Long customerId;
    private BigDecimal originalBalance;
    private BigDecimal calculatedInterest;
    private BigDecimal newBalance;
    private LocalDateTime calculatedAt;

    /**
     * Constructor desde una cuenta y su interés calculado.
     */
    public AccountInterestData(Account account, BigDecimal interest) {
        this.accountId = account.getId();
        this.accountNumber = account.getAccountNumber();
        this.accountType = account.getAccountType();
        this.customerId = account.getCustomerId();
        this.originalBalance = account.getBalance();
        this.calculatedInterest = interest;
        this.newBalance = originalBalance.add(interest);
        this.calculatedAt = LocalDateTime.now();
    }

    /**
     * Indica si se debe aplicar el interés (interés > 0).
     */
    public boolean shouldApplyInterest() {
        return calculatedInterest != null &&
               calculatedInterest.compareTo(BigDecimal.ZERO) > 0;
    }
}