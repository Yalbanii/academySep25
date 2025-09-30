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
@Externalized("banco.transfer.completed::#{#this.transactionId}")
public class TransferCompletedEvent {
    private String transactionId;
    private String sourceAccountNumber;
    private String targetAccountNumber;
    private BigDecimal amount;
    private String sourceCustomerEmail;
    private String targetCustomerEmail;
    private LocalDateTime timestamp;
}
