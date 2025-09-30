package com.xideral.banco.account.dto;

import com.xideral.banco.account.model.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private Long customerId;
    private Account.AccountType accountType;
    private BigDecimal balance;
    private Account.AccountStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AccountResponse fromEntity(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getCustomerId(),
                account.getAccountType(),
                account.getBalance(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}