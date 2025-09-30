package com.xideral.banco.customer.dto;

import com.xideral.banco.customer.model.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CustomerResponse fromEntity(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getStatus().name(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}