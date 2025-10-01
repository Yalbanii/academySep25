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
@Externalized("banco.transaction.completed::#{#this.transactionId}")
public class TransactionCompletedEvent {
    private String transactionId;
    private String accountNumber;
    private String transactionType;
    private BigDecimal amount;
    private BigDecimal newBalance;
    private Long customerId;
    private String customerEmail;
    private LocalDateTime timestamp;
}
