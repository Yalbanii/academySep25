package com.xideral.banco.customer.service;

import com.xideral.banco.customer.model.Customer;
import com.xideral.banco.customer.repository.CustomerRepository;
import com.xideral.banco.events.CustomerCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Customer createCustomer(Customer customer) {
        log.debug("Creating customer with email: {}", customer.getEmail());

        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + customer.getEmail());
        }

        customer.setStatus(Customer.CustomerStatus.ACTIVE);
        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer created successfully with id: {}", savedCustomer.getId());

        // Publicar evento
        CustomerCreatedEvent event = new CustomerCreatedEvent(
                savedCustomer.getId(),
                savedCustomer.getName(),
                savedCustomer.getEmail(),
                LocalDateTime.now()
        );
        eventPublisher.publishEvent(event);
        log.debug("CustomerCreatedEvent published for customer: {}", savedCustomer.getEmail());

        return savedCustomer;
    }

    @Override
    public Customer updateCustomer(Long id, Customer customer) {
        log.debug("Updating customer with id: {}", id);

        Customer existingCustomer = getCustomerById(id);

        // Verificar si el email cambió y si ya existe
        if (!existingCustomer.getEmail().equals(customer.getEmail()) &&
                customerRepository.existsByEmail(customer.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + customer.getEmail());
        }

        existingCustomer.setName(customer.getName());
        existingCustomer.setEmail(customer.getEmail());
        existingCustomer.setPhone(customer.getPhone());

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        log.info("Customer updated successfully with id: {}", updatedCustomer.getId());
        return updatedCustomer;
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        log.debug("Getting customer by id: {}", id);
        return customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        log.debug("Getting all customers");
        return customerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> getCustomersByStatus(Customer.CustomerStatus status) {
        log.debug("Getting customers by status: {}", status);
        return customerRepository.findByStatus(status);
    }

    @Override
    public void deleteCustomer(Long id) {
        log.debug("Deleting customer with id: {}", id);

        Customer customer = getCustomerById(id);
        customer.setStatus(Customer.CustomerStatus.INACTIVE);
        customerRepository.save(customer);
        log.info("Customer soft deleted (deactivated) with id: {}", id);
    }

    @Override
    public Customer activateCustomer(Long id) {
        log.debug("Activating customer with id: {}", id);

        Customer customer = getCustomerById(id);
        customer.setStatus(Customer.CustomerStatus.ACTIVE);
        Customer activatedCustomer = customerRepository.save(customer);
        log.info("Customer activated successfully with id: {}", id);
        return activatedCustomer;
    }

    @Override
    public Customer deactivateCustomer(Long id) {
        log.debug("Deactivating customer with id: {}", id);

        Customer customer = getCustomerById(id);
        customer.setStatus(Customer.CustomerStatus.INACTIVE);
        Customer deactivatedCustomer = customerRepository.save(customer);
        log.info("Customer deactivated successfully with id: {}", id);
        return deactivatedCustomer;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }
}