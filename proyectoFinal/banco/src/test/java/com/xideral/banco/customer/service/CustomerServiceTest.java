package com.xideral.banco.customer.service;

import com.xideral.banco.customer.model.Customer;
import com.xideral.banco.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("Juan Perez");
        testCustomer.setEmail("juan.perez@example.com");
        testCustomer.setPhone("5512345678");
        testCustomer.setStatus(Customer.CustomerStatus.ACTIVE);
    }

    @Test
    void shouldCreateCustomerSuccessfully() {
        // Given
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When
        Customer createdCustomer = customerService.createCustomer(testCustomer);

        // Then
        assertThat(createdCustomer).isNotNull();
        assertThat(createdCustomer.getStatus()).isEqualTo(Customer.CustomerStatus.ACTIVE);
        verify(customerRepository).existsByEmail("juan.perez@example.com");
        verify(customerRepository).save(testCustomer);
    }

    @Test
    void shouldThrowExceptionWhenCreatingCustomerWithDuplicateEmail() {
        // Given
        when(customerRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> customerService.createCustomer(testCustomer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");

        verify(customerRepository).existsByEmail("juan.perez@example.com");
        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldGetCustomerByIdSuccessfully() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        // When
        Customer foundCustomer = customerService.getCustomerById(1L);

        // Then
        assertThat(foundCustomer).isNotNull();
        assertThat(foundCustomer.getId()).isEqualTo(1L);
        assertThat(foundCustomer.getName()).isEqualTo("Juan Perez");
        verify(customerRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {
        // Given
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> customerService.getCustomerById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer not found");

        verify(customerRepository).findById(999L);
    }

    @Test
    void shouldGetAllCustomers() {
        // Given
        Customer customer2 = new Customer();
        customer2.setId(2L);
        customer2.setName("Maria Garcia");
        customer2.setEmail("maria.garcia@example.com");
        customer2.setPhone("5598765432");

        List<Customer> customers = Arrays.asList(testCustomer, customer2);
        when(customerRepository.findAll()).thenReturn(customers);

        // When
        List<Customer> foundCustomers = customerService.getAllCustomers();

        // Then
        assertThat(foundCustomers).hasSize(2);
        verify(customerRepository).findAll();
    }

    @Test
    void shouldGetCustomersByStatus() {
        // Given
        when(customerRepository.findByStatus(Customer.CustomerStatus.ACTIVE))
                .thenReturn(Arrays.asList(testCustomer));

        // When
        List<Customer> activeCustomers = customerService.getCustomersByStatus(Customer.CustomerStatus.ACTIVE);

        // Then
        assertThat(activeCustomers).hasSize(1);
        assertThat(activeCustomers.get(0).getStatus()).isEqualTo(Customer.CustomerStatus.ACTIVE);
        verify(customerRepository).findByStatus(Customer.CustomerStatus.ACTIVE);
    }

    @Test
    void shouldUpdateCustomerSuccessfully() {
        // Given
        Customer updatedData = new Customer();
        updatedData.setName("Juan Carlos Perez");
        updatedData.setEmail("juan.perez@example.com");
        updatedData.setPhone("5599998888");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When
        Customer updatedCustomer = customerService.updateCustomer(1L, updatedData);

        // Then
        assertThat(updatedCustomer.getName()).isEqualTo("Juan Carlos Perez");
        verify(customerRepository).findById(1L);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithDuplicateEmail() {
        // Given
        Customer updatedData = new Customer();
        updatedData.setName("Juan Perez");
        updatedData.setEmail("otro.email@example.com");
        updatedData.setPhone("5512345678");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.existsByEmail("otro.email@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> customerService.updateCustomer(1L, updatedData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");

        verify(customerRepository).findById(1L);
        verify(customerRepository).existsByEmail("otro.email@example.com");
        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldDeleteCustomer() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When
        customerService.deleteCustomer(1L);

        // Then
        verify(customerRepository).findById(1L);
        verify(customerRepository).save(testCustomer);
        assertThat(testCustomer.getStatus()).isEqualTo(Customer.CustomerStatus.INACTIVE);
    }

    @Test
    void shouldActivateCustomer() {
        // Given
        testCustomer.setStatus(Customer.CustomerStatus.INACTIVE);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When
        Customer activatedCustomer = customerService.activateCustomer(1L);

        // Then
        assertThat(activatedCustomer.getStatus()).isEqualTo(Customer.CustomerStatus.ACTIVE);
        verify(customerRepository).findById(1L);
        verify(customerRepository).save(testCustomer);
    }

    @Test
    void shouldDeactivateCustomer() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When
        Customer deactivatedCustomer = customerService.deactivateCustomer(1L);

        // Then
        assertThat(deactivatedCustomer.getStatus()).isEqualTo(Customer.CustomerStatus.INACTIVE);
        verify(customerRepository).findById(1L);
        verify(customerRepository).save(testCustomer);
    }

    @Test
    void shouldCheckIfEmailExists() {
        // Given
        when(customerRepository.existsByEmail("juan.perez@example.com")).thenReturn(true);

        // When
        boolean exists = customerService.existsByEmail("juan.perez@example.com");

        // Then
        assertThat(exists).isTrue();
        verify(customerRepository).existsByEmail("juan.perez@example.com");
    }
}