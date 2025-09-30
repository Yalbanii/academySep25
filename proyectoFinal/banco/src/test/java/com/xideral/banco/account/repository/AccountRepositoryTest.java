package com.xideral.banco.account.repository;

import com.xideral.banco.account.model.Account;
import com.xideral.banco.customer.model.Customer;
import com.xideral.banco.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer testCustomer;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        // Crear cliente de prueba
        testCustomer = new Customer();
        testCustomer.setName("Juan Perez");
        testCustomer.setEmail("juan.perez@test.com");
        testCustomer.setPhone("5512345678");
        testCustomer.setStatus(Customer.CustomerStatus.ACTIVE);
        testCustomer = customerRepository.save(testCustomer);

        // Crear cuenta de prueba
        testAccount = new Account();
        testAccount.setAccountNumber("400012345678");
        testAccount.setCustomerId(testCustomer.getId());
        testAccount.setAccountType(Account.AccountType.CHECKING);
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setStatus(Account.AccountStatus.ACTIVE);
    }

    @Test
    void shouldSaveAccount() {
        // When
        Account savedAccount = accountRepository.save(testAccount);

        // Then
        assertThat(savedAccount.getId()).isNotNull();
        assertThat(savedAccount.getAccountNumber()).isEqualTo("400012345678");
        assertThat(savedAccount.getBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(savedAccount.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindAccountByAccountNumber() {
        // Given
        accountRepository.save(testAccount);

        // When
        Optional<Account> found = accountRepository.findByAccountNumber("400012345678");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getAccountNumber()).isEqualTo("400012345678");
        assertThat(found.get().getCustomerId()).isEqualTo(testCustomer.getId());
    }

    @Test
    void shouldReturnEmptyWhenAccountNumberNotFound() {
        // When
        Optional<Account> found = accountRepository.findByAccountNumber("999999999999");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenAccountNumberExists() {
        // Given
        accountRepository.save(testAccount);

        // When
        boolean exists = accountRepository.existsByAccountNumber("400012345678");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenAccountNumberDoesNotExist() {
        // When
        boolean exists = accountRepository.existsByAccountNumber("999999999999");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void shouldFindAccountsByCustomerId() {
        // Given
        Account account2 = new Account();
        account2.setAccountNumber("400087654321");
        account2.setCustomerId(testCustomer.getId());
        account2.setAccountType(Account.AccountType.SAVINGS);
        account2.setBalance(new BigDecimal("2000.00"));
        account2.setStatus(Account.AccountStatus.ACTIVE);

        accountRepository.save(testAccount);
        accountRepository.save(account2);

        // When
        List<Account> accounts = accountRepository.findByCustomerId(testCustomer.getId());

        // Then
        assertThat(accounts).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldFindAccountsByStatus() {
        // Given
        Account closedAccount = new Account();
        closedAccount.setAccountNumber("400099999999");
        closedAccount.setCustomerId(testCustomer.getId());
        closedAccount.setAccountType(Account.AccountType.CHECKING);
        closedAccount.setBalance(BigDecimal.ZERO);
        closedAccount.setStatus(Account.AccountStatus.CLOSED);

        accountRepository.save(testAccount);
        accountRepository.save(closedAccount);

        // When
        List<Account> activeAccounts = accountRepository.findByStatus(Account.AccountStatus.ACTIVE);
        List<Account> closedAccounts = accountRepository.findByStatus(Account.AccountStatus.CLOSED);

        // Then
        assertThat(activeAccounts).hasSizeGreaterThanOrEqualTo(1);
        assertThat(closedAccounts).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void shouldFindAccountsByAccountType() {
        // Given
        Account savingsAccount = new Account();
        savingsAccount.setAccountNumber("400011111111");
        savingsAccount.setCustomerId(testCustomer.getId());
        savingsAccount.setAccountType(Account.AccountType.SAVINGS);
        savingsAccount.setBalance(new BigDecimal("5000.00"));
        savingsAccount.setStatus(Account.AccountStatus.ACTIVE);

        accountRepository.save(testAccount); // CHECKING
        accountRepository.save(savingsAccount); // SAVINGS

        // When
        List<Account> checkingAccounts = accountRepository.findByAccountType(Account.AccountType.CHECKING);
        List<Account> savingsAccounts = accountRepository.findByAccountType(Account.AccountType.SAVINGS);

        // Then
        assertThat(checkingAccounts).hasSizeGreaterThanOrEqualTo(1);
        assertThat(savingsAccounts).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void shouldFindActiveAccountsByCustomerId() {
        // Given
        Account closedAccount = new Account();
        closedAccount.setAccountNumber("400022222222");
        closedAccount.setCustomerId(testCustomer.getId());
        closedAccount.setAccountType(Account.AccountType.CHECKING);
        closedAccount.setBalance(BigDecimal.ZERO);
        closedAccount.setStatus(Account.AccountStatus.CLOSED);

        accountRepository.save(testAccount); // ACTIVE
        accountRepository.save(closedAccount); // CLOSED

        // When
        List<Account> activeAccounts = accountRepository.findActiveAccountsByCustomerId(testCustomer.getId());

        // Then
        assertThat(activeAccounts).hasSizeGreaterThanOrEqualTo(1);
        assertThat(activeAccounts).allMatch(acc -> acc.getStatus() == Account.AccountStatus.ACTIVE);
    }

    @Test
    void shouldCountAccountsByCustomerId() {
        // Given
        Account account2 = new Account();
        account2.setAccountNumber("400033333333");
        account2.setCustomerId(testCustomer.getId());
        account2.setAccountType(Account.AccountType.SAVINGS);
        account2.setBalance(new BigDecimal("3000.00"));
        account2.setStatus(Account.AccountStatus.ACTIVE);

        accountRepository.save(testAccount);
        accountRepository.save(account2);

        // When
        long count = accountRepository.countByCustomerId(testCustomer.getId());

        // Then
        assertThat(count).isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldFindAccountsWithMinBalance() {
        // Given
        Account lowBalanceAccount = new Account();
        lowBalanceAccount.setAccountNumber("400044444444");
        lowBalanceAccount.setCustomerId(testCustomer.getId());
        lowBalanceAccount.setAccountType(Account.AccountType.CHECKING);
        lowBalanceAccount.setBalance(new BigDecimal("50.00"));
        lowBalanceAccount.setStatus(Account.AccountStatus.ACTIVE);

        accountRepository.save(testAccount); // 1000.00
        accountRepository.save(lowBalanceAccount); // 50.00

        // When
        List<Account> accounts = accountRepository.findAccountsWithMinBalance(new BigDecimal("500.00"));

        // Then
        assertThat(accounts).hasSizeGreaterThanOrEqualTo(1);
        assertThat(accounts).allMatch(acc -> acc.getBalance().compareTo(new BigDecimal("500.00")) >= 0);
    }

    @Test
    void shouldUpdateAccount() {
        // Given
        Account savedAccount = accountRepository.save(testAccount);

        // When
        savedAccount.setBalance(new BigDecimal("1500.00"));
        Account updatedAccount = accountRepository.save(savedAccount);

        // Then
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(updatedAccount.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldDeleteAccount() {
        // Given
        Account savedAccount = accountRepository.save(testAccount);
        Long accountId = savedAccount.getId();

        // When
        accountRepository.deleteById(accountId);

        // Then
        Optional<Account> deletedAccount = accountRepository.findById(accountId);
        assertThat(deletedAccount).isEmpty();
    }
}