package com.xideral.banco.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.modulith.events.Externalized;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Externalized("banco.account.created::#{#this.accountNumber}")
public class AccountCreatedEvent {
    private Long accountId;
    private String accountNumber;
    private String accountType;
    private BigDecimal initialBalance;
    private Long customerId;
    private String customerEmail;
    private LocalDateTime createdAt;
}
