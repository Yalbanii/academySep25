package com.xideral.banco.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xideral.banco.customer.dto.CustomerRequest;
import com.xideral.banco.customer.model.Customer;
import com.xideral.banco.customer.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@org.springframework.test.context.ActiveProfiles("test")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    private Customer testCustomer;
    private CustomerRequest customerRequest;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("Juan Perez");
        testCustomer.setEmail("juan.perez@example.com");
        testCustomer.setPhone("5512345678");
        testCustomer.setStatus(Customer.CustomerStatus.ACTIVE);

        customerRequest = new CustomerRequest();
        customerRequest.setName("Juan Perez");
        customerRequest.setEmail("juan.perez@example.com");
        customerRequest.setPhone("5512345678");
    }

    @Test
    void shouldCreateCustomerSuccessfully() throws Exception {
        // Given
        when(customerService.createCustomer(any(Customer.class))).thenReturn(testCustomer);

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Juan Perez"))
                .andExpect(jsonPath("$.email").value("juan.perez@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(customerService).createCustomer(any(Customer.class));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingCustomerWithInvalidData() throws Exception {
        // Given
        CustomerRequest invalidRequest = new CustomerRequest();
        invalidRequest.setName("");
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPhone("123");

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any());
    }

    @Test
    void shouldGetCustomerByIdSuccessfully() throws Exception {
        // Given
        when(customerService.getCustomerById(1L)).thenReturn(testCustomer);

        // When & Then
        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Juan Perez"))
                .andExpect(jsonPath("$.email").value("juan.perez@example.com"));

        verify(customerService).getCustomerById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenCustomerDoesNotExist() throws Exception {
        // Given
        when(customerService.getCustomerById(999L))
                .thenThrow(new IllegalArgumentException("Customer not found"));

        // When & Then
        mockMvc.perform(get("/api/customers/999"))
                .andExpect(status().isBadRequest());

        verify(customerService).getCustomerById(999L);
    }

    @Test
    void shouldGetAllCustomers() throws Exception {
        // Given
        Customer customer2 = new Customer();
        customer2.setId(2L);
        customer2.setName("Maria Garcia");
        customer2.setEmail("maria.garcia@example.com");
        customer2.setPhone("5598765432");
        customer2.setStatus(Customer.CustomerStatus.ACTIVE);

        List<Customer> customers = Arrays.asList(testCustomer, customer2);
        when(customerService.getAllCustomers()).thenReturn(customers);

        // When & Then
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Juan Perez"))
                .andExpect(jsonPath("$[1].name").value("Maria Garcia"));

        verify(customerService).getAllCustomers();
    }

    @Test
    void shouldGetCustomersByStatus() throws Exception {
        // Given
        when(customerService.getCustomersByStatus(Customer.CustomerStatus.ACTIVE))
                .thenReturn(Arrays.asList(testCustomer));

        // When & Then
        mockMvc.perform(get("/api/customers/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(customerService).getCustomersByStatus(Customer.CustomerStatus.ACTIVE);
    }

    @Test
    void shouldUpdateCustomerSuccessfully() throws Exception {
        // Given
        when(customerService.updateCustomer(eq(1L), any(Customer.class))).thenReturn(testCustomer);

        // When & Then
        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Juan Perez"));

        verify(customerService).updateCustomer(eq(1L), any(Customer.class));
    }

    @Test
    void shouldDeleteCustomerSuccessfully() throws Exception {
        // Given
        doNothing().when(customerService).deleteCustomer(1L);

        // When & Then
        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());

        verify(customerService).deleteCustomer(1L);
    }

    @Test
    void shouldActivateCustomerSuccessfully() throws Exception {
        // Given
        when(customerService.activateCustomer(1L)).thenReturn(testCustomer);

        // When & Then
        mockMvc.perform(patch("/api/customers/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(customerService).activateCustomer(1L);
    }

    @Test
    void shouldDeactivateCustomerSuccessfully() throws Exception {
        // Given
        testCustomer.setStatus(Customer.CustomerStatus.INACTIVE);
        when(customerService.deactivateCustomer(1L)).thenReturn(testCustomer);

        // When & Then
        mockMvc.perform(patch("/api/customers/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        verify(customerService).deactivateCustomer(1L);
    }
}