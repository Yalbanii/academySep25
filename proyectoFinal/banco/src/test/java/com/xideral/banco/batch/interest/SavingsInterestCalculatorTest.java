package com.xideral.banco.batch.interest;

import com.xideral.banco.account.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SavingsInterestCalculatorTest {

    private SavingsInterestCalculator calculator;
    private Account savingsAccount;

    @BeforeEach
    void setUp() {
        calculator = new SavingsInterestCalculator();

        savingsAccount = new Account();
        savingsAccount.setId(1L);
        savingsAccount.setAccountNumber("400012345678");
        savingsAccount.setAccountType(Account.AccountType.SAVINGS);
        savingsAccount.setStatus(Account.AccountStatus.ACTIVE);
        savingsAccount.setBalance(new BigDecimal("1000.00"));
        savingsAccount.setCustomerId(1L);
        savingsAccount.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void shouldCalculateInterestForActiveSavingsAccount() {
        // Given: Account with balance 1000.00
        savingsAccount.setBalance(new BigDecimal("1000.00"));

        // When
        BigDecimal interest = calculator.calculateInterest(savingsAccount);

        // Then: Interest = 1000 * 0.004166667 = 4.17 (rounded)
        assertThat(interest).isEqualByComparingTo(new BigDecimal("4.17"));
    }

    @Test
    void shouldReturnZeroForInactiveAccount() {
        // Given: Inactive/closed account
        savingsAccount.setStatus(Account.AccountStatus.CLOSED);
        savingsAccount.setBalance(new BigDecimal("1000.00"));

        // When
        BigDecimal interest = calculator.calculateInterest(savingsAccount);

        // Then
        assertThat(interest).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnZeroForZeroBalance() {
        // Given: Account with zero balance
        savingsAccount.setBalance(BigDecimal.ZERO);

        // When
        BigDecimal interest = calculator.calculateInterest(savingsAccount);

        // Then
        assertThat(interest).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnZeroForNegativeBalance() {
        // Given: Account with negative balance
        savingsAccount.setBalance(new BigDecimal("-100.00"));

        // When
        BigDecimal interest = calculator.calculateInterest(savingsAccount);

        // Then
        assertThat(interest).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldThrowExceptionForCheckingAccount() {
        // Given: Checking account instead of savings
        savingsAccount.setAccountType(Account.AccountType.CHECKING);

        // When/Then
        assertThatThrownBy(() -> calculator.calculateInterest(savingsAccount))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("SavingsInterestCalculator only applies to SAVINGS accounts");
    }

    @Test
    void shouldCalculateInterestForLargeBalance() {
        // Given: Account with large balance
        savingsAccount.setBalance(new BigDecimal("100000.00"));

        // When
        BigDecimal interest = calculator.calculateInterest(savingsAccount);

        // Then: Interest = 100000 * 0.004166667 = 416.67 (rounded)
        assertThat(interest).isEqualByComparingTo(new BigDecimal("416.67"));
    }

    @Test
    void shouldCalculateInterestForSmallBalance() {
        // Given: Account with small balance
        savingsAccount.setBalance(new BigDecimal("10.00"));

        // When
        BigDecimal interest = calculator.calculateInterest(savingsAccount);

        // Then: Interest = 10 * 0.004166667 = 0.04 (rounded)
        assertThat(interest).isEqualByComparingTo(new BigDecimal("0.04"));
    }

    @Test
    void shouldReturnCorrectInterestRate() {
        // When
        BigDecimal rate = calculator.getInterestRate();

        // Then: Monthly rate for 5% annual = 0.004166667
        assertThat(rate).isEqualByComparingTo(new BigDecimal("0.004166667"));
    }

    @Test
    void shouldReturnCorrectAccountType() {
        // When
        Account.AccountType type = calculator.getAccountType();

        // Then
        assertThat(type).isEqualTo(Account.AccountType.SAVINGS);
    }

    @Test
    void shouldRoundInterestToTwoDecimals() {
        // Given: Balance that results in more than 2 decimal places
        savingsAccount.setBalance(new BigDecimal("123.45"));

        // When
        BigDecimal interest = calculator.calculateInterest(savingsAccount);

        // Then: Should be rounded to 2 decimal places
        assertThat(interest.scale()).isEqualTo(2);
    }
}
