package com.xideral.banco.batch.interest;

import com.xideral.banco.account.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CheckingInterestCalculatorTest {

    private CheckingInterestCalculator calculator;
    private Account checkingAccount;

    @BeforeEach
    void setUp() {
        calculator = new CheckingInterestCalculator();

        checkingAccount = new Account();
        checkingAccount.setId(1L);
        checkingAccount.setAccountNumber("400012345678");
        checkingAccount.setAccountType(Account.AccountType.CHECKING);
        checkingAccount.setStatus(Account.AccountStatus.ACTIVE);
        checkingAccount.setBalance(new BigDecimal("1000.00"));
        checkingAccount.setCustomerId(1L);
        checkingAccount.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void shouldCalculateInterestForActiveCheckingAccount() {
        // Given: Account with balance 1000.00
        checkingAccount.setBalance(new BigDecimal("1000.00"));

        // When
        BigDecimal interest = calculator.calculateInterest(checkingAccount);

        // Then: Interest = 1000 * 0.000833333 = 0.83 (rounded)
        assertThat(interest).isEqualByComparingTo(new BigDecimal("0.83"));
    }

    @Test
    void shouldReturnZeroForInactiveAccount() {
        // Given: Inactive/closed account
        checkingAccount.setStatus(Account.AccountStatus.CLOSED);
        checkingAccount.setBalance(new BigDecimal("1000.00"));

        // When
        BigDecimal interest = calculator.calculateInterest(checkingAccount);

        // Then
        assertThat(interest).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnZeroForZeroBalance() {
        // Given: Account with zero balance
        checkingAccount.setBalance(BigDecimal.ZERO);

        // When
        BigDecimal interest = calculator.calculateInterest(checkingAccount);

        // Then
        assertThat(interest).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnZeroForNegativeBalance() {
        // Given: Account with negative balance
        checkingAccount.setBalance(new BigDecimal("-100.00"));

        // When
        BigDecimal interest = calculator.calculateInterest(checkingAccount);

        // Then
        assertThat(interest).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldThrowExceptionForSavingsAccount() {
        // Given: Savings account instead of checking
        checkingAccount.setAccountType(Account.AccountType.SAVINGS);

        // When/Then
        assertThatThrownBy(() -> calculator.calculateInterest(checkingAccount))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("CheckingInterestCalculator only applies to CHECKING accounts");
    }

    @Test
    void shouldCalculateInterestForLargeBalance() {
        // Given: Account with large balance
        checkingAccount.setBalance(new BigDecimal("100000.00"));

        // When
        BigDecimal interest = calculator.calculateInterest(checkingAccount);

        // Then: Interest = 100000 * 0.000833333 = 83.33 (rounded)
        assertThat(interest).isEqualByComparingTo(new BigDecimal("83.33"));
    }

    @Test
    void shouldCalculateInterestForSmallBalance() {
        // Given: Account with small balance
        checkingAccount.setBalance(new BigDecimal("10.00"));

        // When
        BigDecimal interest = calculator.calculateInterest(checkingAccount);

        // Then: Interest = 10 * 0.000833333 = 0.01 (rounded)
        assertThat(interest).isEqualByComparingTo(new BigDecimal("0.01"));
    }

    @Test
    void shouldReturnCorrectInterestRate() {
        // When
        BigDecimal rate = calculator.getInterestRate();

        // Then: Monthly rate for 1% annual = 0.000833333
        assertThat(rate).isEqualByComparingTo(new BigDecimal("0.000833333"));
    }

    @Test
    void shouldReturnCorrectAccountType() {
        // When
        Account.AccountType type = calculator.getAccountType();

        // Then
        assertThat(type).isEqualTo(Account.AccountType.CHECKING);
    }

    @Test
    void shouldRoundInterestToTwoDecimals() {
        // Given: Balance that results in more than 2 decimal places
        checkingAccount.setBalance(new BigDecimal("123.45"));

        // When
        BigDecimal interest = calculator.calculateInterest(checkingAccount);

        // Then: Should be rounded to 2 decimal places
        assertThat(interest.scale()).isEqualTo(2);
    }
}
