package com.xideral.banco.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xideral.banco.account.dto.AccountRequest;
import com.xideral.banco.account.dto.TransactionRequest;
import com.xideral.banco.account.dto.TransferRequest;
import com.xideral.banco.account.model.Account;
import com.xideral.banco.account.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    private Account testAccount;
    private AccountRequest accountRequest;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setAccountNumber("400012345678");
        testAccount.setCustomerId(1L);
        testAccount.setAccountType(Account.AccountType.CHECKING);
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setStatus(Account.AccountStatus.ACTIVE);

        accountRequest = new AccountRequest();
        accountRequest.setCustomerId(1L);
        accountRequest.setAccountType(Account.AccountType.CHECKING);
        accountRequest.setInitialBalance(BigDecimal.ZERO);
    }

    @Test
    void shouldCreateAccountSuccessfully() throws Exception {
        // Given
        when(accountService.createAccount(any(Account.class))).thenReturn(testAccount);

        // When & Then
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountNumber").value("400012345678"))
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.accountType").value("CHECKING"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(accountService).createAccount(any(Account.class));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingAccountWithInvalidData() throws Exception {
        // Given
        AccountRequest invalidRequest = new AccountRequest();
        invalidRequest.setCustomerId(null); // Invalid
        invalidRequest.setAccountType(null); // Invalid

        // When & Then
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(accountService, never()).createAccount(any());
    }

    @Test
    void shouldGetAccountByIdSuccessfully() throws Exception {
        // Given
        when(accountService.getAccountById(1L)).thenReturn(testAccount);

        // When & Then
        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountNumber").value("400012345678"))
                .andExpect(jsonPath("$.balance").value(1000.00));

        verify(accountService).getAccountById(1L);
    }

    @Test
    void shouldGetAccountByAccountNumber() throws Exception {
        // Given
        when(accountService.getAccountByAccountNumber("400012345678")).thenReturn(testAccount);

        // When & Then
        mockMvc.perform(get("/api/accounts/number/400012345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("400012345678"));

        verify(accountService).getAccountByAccountNumber("400012345678");
    }

    @Test
    void shouldGetAllAccounts() throws Exception {
        // Given
        Account account2 = new Account();
        account2.setId(2L);
        account2.setAccountNumber("400087654321");
        account2.setCustomerId(1L);
        account2.setAccountType(Account.AccountType.SAVINGS);
        account2.setBalance(new BigDecimal("2000.00"));
        account2.setStatus(Account.AccountStatus.ACTIVE);

        when(accountService.getAllAccounts()).thenReturn(Arrays.asList(testAccount, account2));

        // When & Then
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].accountNumber").value("400012345678"))
                .andExpect(jsonPath("$[1].accountNumber").value("400087654321"));

        verify(accountService).getAllAccounts();
    }

    @Test
    void shouldGetAccountsByCustomerId() throws Exception {
        // Given
        when(accountService.getAccountsByCustomerId(1L)).thenReturn(Arrays.asList(testAccount));

        // When & Then
        mockMvc.perform(get("/api/accounts/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerId").value(1));

        verify(accountService).getAccountsByCustomerId(1L);
    }

    @Test
    void shouldGetActiveAccountsByCustomerId() throws Exception {
        // Given
        when(accountService.getActiveAccountsByCustomerId(1L)).thenReturn(Arrays.asList(testAccount));

        // When & Then
        mockMvc.perform(get("/api/accounts/customer/1/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(accountService).getActiveAccountsByCustomerId(1L);
    }

    @Test
    void shouldGetAccountsByStatus() throws Exception {
        // Given
        when(accountService.getAccountsByStatus(Account.AccountStatus.ACTIVE))
                .thenReturn(Arrays.asList(testAccount));

        // When & Then
        mockMvc.perform(get("/api/accounts/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(accountService).getAccountsByStatus(Account.AccountStatus.ACTIVE);
    }

    @Test
    void shouldGetAccountsByType() throws Exception {
        // Given
        when(accountService.getAccountsByType(Account.AccountType.CHECKING))
                .thenReturn(Arrays.asList(testAccount));

        // When & Then
        mockMvc.perform(get("/api/accounts/type/CHECKING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].accountType").value("CHECKING"));

        verify(accountService).getAccountsByType(Account.AccountType.CHECKING);
    }

    @Test
    void shouldDepositSuccessfully() throws Exception {
        // Given
        testAccount.setBalance(new BigDecimal("1500.00"));
        TransactionRequest request = new TransactionRequest("400012345678", new BigDecimal("500.00"), "Deposit");

        when(accountService.deposit(eq("400012345678"), eq(new BigDecimal("500.00"))))
                .thenReturn(testAccount);

        // When & Then
        mockMvc.perform(post("/api/accounts/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1500.00));

        verify(accountService).deposit("400012345678", new BigDecimal("500.00"));
    }

    @Test
    void shouldWithdrawSuccessfully() throws Exception {
        // Given
        testAccount.setBalance(new BigDecimal("500.00"));
        TransactionRequest request = new TransactionRequest("400012345678", new BigDecimal("500.00"), "Withdrawal");

        when(accountService.withdraw(eq("400012345678"), eq(new BigDecimal("500.00"))))
                .thenReturn(testAccount);

        // When & Then
        mockMvc.perform(post("/api/accounts/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(500.00));

        verify(accountService).withdraw("400012345678", new BigDecimal("500.00"));
    }

    @Test
    void shouldTransferSuccessfully() throws Exception {
        // Given
        TransferRequest request = new TransferRequest(
                "400012345678",
                "400087654321",
                new BigDecimal("300.00"),
                "Transfer"
        );

        doNothing().when(accountService).transfer(
                eq("400012345678"),
                eq("400087654321"),
                eq(new BigDecimal("300.00"))
        );

        // When & Then
        mockMvc.perform(post("/api/accounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(accountService).transfer("400012345678", "400087654321", new BigDecimal("300.00"));
    }

    @Test
    void shouldActivateAccountSuccessfully() throws Exception {
        // Given
        when(accountService.activateAccount(1L)).thenReturn(testAccount);

        // When & Then
        mockMvc.perform(patch("/api/accounts/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(accountService).activateAccount(1L);
    }

    @Test
    void shouldCloseAccountSuccessfully() throws Exception {
        // Given
        testAccount.setStatus(Account.AccountStatus.CLOSED);
        testAccount.setBalance(BigDecimal.ZERO);
        when(accountService.closeAccount(1L)).thenReturn(testAccount);

        // When & Then
        mockMvc.perform(patch("/api/accounts/1/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        verify(accountService).closeAccount(1L);
    }

    @Test
    void shouldDeleteAccountSuccessfully() throws Exception {
        // Given
        doNothing().when(accountService).deleteAccount(1L);

        // When & Then
        mockMvc.perform(delete("/api/accounts/1"))
                .andExpect(status().isNoContent());

        verify(accountService).deleteAccount(1L);
    }

    @Test
    void shouldCountAccountsByCustomerId() throws Exception {
        // Given
        when(accountService.countAccountsByCustomerId(1L)).thenReturn(2L);

        // When & Then
        mockMvc.perform(get("/api/accounts/customer/1/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));

        verify(accountService).countAccountsByCustomerId(1L);
    }
}