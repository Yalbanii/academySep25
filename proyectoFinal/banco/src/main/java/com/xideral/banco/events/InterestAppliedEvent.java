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
@Externalized("banco.interest.applied::#{#this.accountNumber}")
public class InterestAppliedEvent {
    private String accountNumber;
    private String accountType;
    private BigDecimal interestAmount;
    private BigDecimal previousBalance;
    private BigDecimal newBalance;
    private Long customerId;
    private String customerEmail;
    private LocalDateTime timestamp;
}
