package com.xideral.banco.customer.service;

import com.xideral.banco.customer.model.Customer;

import java.util.List;

public interface CustomerService {

    Customer createCustomer(Customer customer);

    Customer updateCustomer(Long id, Customer customer);

    Customer getCustomerById(Long id);

    List<Customer> getAllCustomers();

    List<Customer> getCustomersByStatus(Customer.CustomerStatus status);

    void deleteCustomer(Long id);

    Customer activateCustomer(Long id);

    Customer deactivateCustomer(Long id);

    boolean existsByEmail(String email);
}