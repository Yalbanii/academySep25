package com.xideral.banco.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.modulith.events.Externalized;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Externalized("banco.customer.created::#{#this.customerId}")
public class CustomerCreatedEvent {
    private Long customerId;
    private String fullName;
    private String email;
    private LocalDateTime createdAt;
}
