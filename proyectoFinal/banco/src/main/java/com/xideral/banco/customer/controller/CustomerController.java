package com.xideral.banco.customer.controller;

import com.xideral.banco.customer.dto.CustomerRequest;
import com.xideral.banco.customer.dto.CustomerResponse;
import com.xideral.banco.customer.model.Customer;
import com.xideral.banco.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "Customer management APIs")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Create a new customer")
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());

        Customer createdCustomer = customerService.createCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomerResponse.fromEntity(createdCustomer));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(CustomerResponse.fromEntity(customer));
    }

    @GetMapping
    @Operation(summary = "Get all customers")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        List<CustomerResponse> response = customers.stream()
                .map(CustomerResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get customers by status")
    public ResponseEntity<List<CustomerResponse>> getCustomersByStatus(
            @PathVariable Customer.CustomerStatus status) {
        List<Customer> customers = customerService.getCustomersByStatus(status);
        List<CustomerResponse> response = customers.stream()
                .map(CustomerResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());

        Customer updatedCustomer = customerService.updateCustomer(id, customer);
        return ResponseEntity.ok(CustomerResponse.fromEntity(updatedCustomer));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer (soft delete)")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate customer")
    public ResponseEntity<CustomerResponse> activateCustomer(@PathVariable Long id) {
        Customer customer = customerService.activateCustomer(id);
        return ResponseEntity.ok(CustomerResponse.fromEntity(customer));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate customer")
    public ResponseEntity<CustomerResponse> deactivateCustomer(@PathVariable Long id) {
        Customer customer = customerService.deactivateCustomer(id);
        return ResponseEntity.ok(CustomerResponse.fromEntity(customer));
    }
}