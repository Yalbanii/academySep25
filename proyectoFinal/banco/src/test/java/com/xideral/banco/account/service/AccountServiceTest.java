package com.xideral.banco.account.service;

import com.xideral.banco.account.model.Account;
import com.xideral.banco.account.repository.AccountRepository;
import com.xideral.banco.customer.model.Customer;
import com.xideral.banco.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Customer testCustomer;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("Juan Perez");
        testCustomer.setEmail("juan.perez@example.com");
        testCustomer.setPhone("5512345678");
        testCustomer.setStatus(Customer.CustomerStatus.ACTIVE);

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setAccountNumber("400012345678");
        testAccount.setCustomerId(1L);
        testAccount.setAccountType(Account.AccountType.CHECKING);
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setStatus(Account.AccountStatus.ACTIVE);
    }

    @Test
    void shouldCreateAccountSuccessfully() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account newAccount = new Account();
        newAccount.setCustomerId(1L);
        newAccount.setAccountType(Account.AccountType.CHECKING);
        newAccount.setBalance(BigDecimal.ZERO);

        // When
        Account createdAccount = accountService.createAccount(newAccount);

        // Then
        assertThat(createdAccount).isNotNull();
        assertThat(createdAccount.getStatus()).isEqualTo(Account.AccountStatus.ACTIVE);
        verify(customerRepository).findById(1L);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingAccountForNonExistentCustomer() {
        // Given
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        Account newAccount = new Account();
        newAccount.setCustomerId(999L);
        newAccount.setAccountType(Account.AccountType.CHECKING);

        // When & Then
        assertThatThrownBy(() -> accountService.createAccount(newAccount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer not found");

        verify(customerRepository).findById(999L);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCreatingAccountForInactiveCustomer() {
        // Given
        testCustomer.setStatus(Customer.CustomerStatus.INACTIVE);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        Account newAccount = new Account();
        newAccount.setCustomerId(1L);
        newAccount.setAccountType(Account.AccountType.CHECKING);

        // When & Then
        assertThatThrownBy(() -> accountService.createAccount(newAccount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer is not active");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenInitialBalanceIsNegative() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        Account newAccount = new Account();
        newAccount.setCustomerId(1L);
        newAccount.setAccountType(Account.AccountType.CHECKING);
        newAccount.setBalance(new BigDecimal("-100.00"));

        // When & Then
        assertThatThrownBy(() -> accountService.createAccount(newAccount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("balance cannot be negative");
    }

    @Test
    void shouldGetAccountByIdSuccessfully() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // When
        Account foundAccount = accountService.getAccountById(1L);

        // Then
        assertThat(foundAccount).isNotNull();
        assertThat(foundAccount.getId()).isEqualTo(1L);
        assertThat(foundAccount.getAccountNumber()).isEqualTo("400012345678");
        verify(accountRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFoundById() {
        // Given
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> accountService.getAccountById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account not found");

        verify(accountRepository).findById(999L);
    }

    @Test
    void shouldGetAccountByAccountNumber() {
        // Given
        when(accountRepository.findByAccountNumber("400012345678")).thenReturn(Optional.of(testAccount));

        // When
        Account foundAccount = accountService.getAccountByAccountNumber("400012345678");

        // Then
        assertThat(foundAccount).isNotNull();
        assertThat(foundAccount.getAccountNumber()).isEqualTo("400012345678");
        verify(accountRepository).findByAccountNumber("400012345678");
    }

    @Test
    void shouldDepositSuccessfully() {
        // Given
        when(accountRepository.findByAccountNumber("400012345678")).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        Account updatedAccount = accountService.deposit("400012345678", new BigDecimal("500.00"));

        // Then
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));
        verify(accountRepository).save(testAccount);
    }

    @Test
    void shouldThrowExceptionWhenDepositAmountIsZeroOrNegative() {
        // When & Then
        assertThatThrownBy(() -> accountService.deposit("400012345678", BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be greater than zero");

        assertThatThrownBy(() -> accountService.deposit("400012345678", new BigDecimal("-50.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be greater than zero");
    }

    @Test
    void shouldThrowExceptionWhenDepositingToInactiveAccount() {
        // Given
        testAccount.setStatus(Account.AccountStatus.CLOSED);
        when(accountRepository.findByAccountNumber("400012345678")).thenReturn(Optional.of(testAccount));

        // When & Then
        assertThatThrownBy(() -> accountService.deposit("400012345678", new BigDecimal("100.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account is not active");
    }

    @Test
    void shouldWithdrawSuccessfullyFromCheckingAccount() {
        // Given
        when(accountRepository.findByAccountNumber("400012345678")).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        Account updatedAccount = accountService.withdraw("400012345678", new BigDecimal("500.00"));

        // Then
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo(new BigDecimal("500.00"));
        verify(accountRepository).save(testAccount);
    }

    @Test
    void shouldAllowCheckingAccountToReachZeroBalance() {
        // Given - CHECKING account can reach 0
        testAccount.setAccountType(Account.AccountType.CHECKING);
        testAccount.setBalance(new BigDecimal("500.00"));
        when(accountRepository.findByAccountNumber("400012345678")).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        Account updatedAccount = accountService.withdraw("400012345678", new BigDecimal("500.00"));

        // Then
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldEnforceMiniumumBalanceForSavingsAccount() {
        // Given - SAVINGS account requires minimum $100
        testAccount.setAccountType(Account.AccountType.SAVINGS);
        testAccount.setBalance(new BigDecimal("500.00"));
        when(accountRepository.findByAccountNumber("400012345678")).thenReturn(Optional.of(testAccount));

        // When & Then - Cannot withdraw if balance would go below $100
        assertThatThrownBy(() -> accountService.withdraw("400012345678", new BigDecimal("450.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    void shouldThrowExceptionWhenWithdrawingFromInactiveAccount() {
        // Given
        testAccount.setStatus(Account.AccountStatus.CLOSED);
        when(accountRepository.findByAccountNumber("400012345678")).thenReturn(Optional.of(testAccount));

        // When & Then
        assertThatThrownBy(() -> accountService.withdraw("400012345678", new BigDecimal("100.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account is not active");
    }

    @Test
    void shouldTransferSuccessfully() {
        // Given
        Account toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setAccountNumber("400087654321");
        toAccount.setCustomerId(1L);
        toAccount.setAccountType(Account.AccountType.SAVINGS);
        toAccount.setBalance(new BigDecimal("2000.00"));
        toAccount.setStatus(Account.AccountStatus.ACTIVE);

        when(accountRepository.findByAccountNumber("400012345678")).thenReturn(Optional.of(testAccount));
        when(accountRepository.findByAccountNumber("400087654321")).thenReturn(Optional.of(toAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        accountService.transfer("400012345678", "400087654321", new BigDecimal("300.00"));

        // Then
        assertThat(testAccount.getBalance()).isEqualByComparingTo(new BigDecimal("700.00"));
        assertThat(toAccount.getBalance()).isEqualByComparingTo(new BigDecimal("2300.00"));
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void shouldThrowExceptionWhenTransferringToSameAccount() {
        // When & Then
        assertThatThrownBy(() -> accountService.transfer("400012345678", "400012345678", new BigDecimal("100.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot transfer to the same account");
    }

    @Test
    void shouldThrowExceptionWhenTransferAmountExceedsBalance() {
        // Given
        Account toAccount = new Account();
        toAccount.setAccountNumber("400087654321");
        toAccount.setStatus(Account.AccountStatus.ACTIVE);

        when(accountRepository.findByAccountNumber("400012345678")).thenReturn(Optional.of(testAccount));
        when(accountRepository.findByAccountNumber("400087654321")).thenReturn(Optional.of(toAccount));

        // When & Then
        assertThatThrownBy(() -> accountService.transfer("400012345678", "400087654321", new BigDecimal("2000.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    void shouldCloseAccountWithZeroBalance() {
        // Given
        testAccount.setBalance(BigDecimal.ZERO);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        Account closedAccount = accountService.closeAccount(1L);

        // Then
        assertThat(closedAccount.getStatus()).isEqualTo(Account.AccountStatus.CLOSED);
        verify(accountRepository).save(testAccount);
    }

    @Test
    void shouldThrowExceptionWhenClosingAccountWithNonZeroBalance() {
        // Given
        testAccount.setBalance(new BigDecimal("100.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // When & Then
        assertThatThrownBy(() -> accountService.closeAccount(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot close account with non-zero balance");
    }

    @Test
    void shouldGetAccountsByCustomerId() {
        // Given
        Account account2 = new Account();
        account2.setId(2L);
        account2.setAccountNumber("400087654321");
        account2.setCustomerId(1L);

        when(accountRepository.findByCustomerId(1L)).thenReturn(Arrays.asList(testAccount, account2));

        // When
        List<Account> accounts = accountService.getAccountsByCustomerId(1L);

        // Then
        assertThat(accounts).hasSize(2);
        verify(accountRepository).findByCustomerId(1L);
    }
}