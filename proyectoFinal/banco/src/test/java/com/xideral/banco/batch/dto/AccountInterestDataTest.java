package com.xideral.banco.batch.dto;

import com.xideral.banco.account.model.Account;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AccountInterestDataTest {

    @Test
    void shouldCreateAccountInterestDataFromAccount() {
        // Given
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("400012345678");
        account.setAccountType(Account.AccountType.SAVINGS);
        account.setCustomerId(100L);
        account.setBalance(new BigDecimal("1000.00"));
        account.setCreatedAt(LocalDateTime.now());

        BigDecimal interest = new BigDecimal("50.00");

        // When
        AccountInterestData data = new AccountInterestData(account, interest);

        // Then
        assertThat(data.getAccountId()).isEqualTo(1L);
        assertThat(data.getAccountNumber()).isEqualTo("400012345678");
        assertThat(data.getAccountType()).isEqualTo(Account.AccountType.SAVINGS);
        assertThat(data.getCustomerId()).isEqualTo(100L);
        assertThat(data.getOriginalBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(data.getCalculatedInterest()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(data.getNewBalance()).isEqualByComparingTo(new BigDecimal("1050.00"));
        assertThat(data.getCalculatedAt()).isNotNull();
    }

    @Test
    void shouldIndicateInterestShouldBeApplied() {
        // Given
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("400012345678");
        account.setAccountType(Account.AccountType.SAVINGS);
        account.setCustomerId(100L);
        account.setBalance(new BigDecimal("1000.00"));
        account.setCreatedAt(LocalDateTime.now());

        AccountInterestData data = new AccountInterestData(account, new BigDecimal("50.00"));

        // When/Then
        assertThat(data.shouldApplyInterest()).isTrue();
    }

    @Test
    void shouldIndicateInterestShouldNotBeAppliedForZeroInterest() {
        // Given
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("400012345678");
        account.setAccountType(Account.AccountType.SAVINGS);
        account.setCustomerId(100L);
        account.setBalance(new BigDecimal("1000.00"));
        account.setCreatedAt(LocalDateTime.now());

        AccountInterestData data = new AccountInterestData(account, BigDecimal.ZERO);

        // When/Then
        assertThat(data.shouldApplyInterest()).isFalse();
    }

    @Test
    void shouldIndicateInterestShouldNotBeAppliedForNegativeInterest() {
        // Given
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("400012345678");
        account.setAccountType(Account.AccountType.SAVINGS);
        account.setCustomerId(100L);
        account.setBalance(new BigDecimal("1000.00"));
        account.setCreatedAt(LocalDateTime.now());

        AccountInterestData data = new AccountInterestData(account, new BigDecimal("-10.00"));

        // When/Then
        assertThat(data.shouldApplyInterest()).isFalse();
    }

    @Test
    void shouldIndicateInterestShouldNotBeAppliedForNullInterest() {
        // Given
        AccountInterestData data = new AccountInterestData();
        data.setCalculatedInterest(null);

        // When/Then
        assertThat(data.shouldApplyInterest()).isFalse();
    }

    @Test
    void shouldCreateWithNoArgsConstructor() {
        // When
        AccountInterestData data = new AccountInterestData();

        // Then
        assertThat(data).isNotNull();
    }

    @Test
    void shouldCreateWithAllArgsConstructor() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        AccountInterestData data = new AccountInterestData(
            1L,
            "400012345678",
            Account.AccountType.SAVINGS,
            100L,
            new BigDecimal("1000.00"),
            new BigDecimal("50.00"),
            new BigDecimal("1050.00"),
            now
        );

        // Then
        assertThat(data.getAccountId()).isEqualTo(1L);
        assertThat(data.getAccountNumber()).isEqualTo("400012345678");
        assertThat(data.getAccountType()).isEqualTo(Account.AccountType.SAVINGS);
        assertThat(data.getCustomerId()).isEqualTo(100L);
        assertThat(data.getOriginalBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(data.getCalculatedInterest()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(data.getNewBalance()).isEqualByComparingTo(new BigDecimal("1050.00"));
        assertThat(data.getCalculatedAt()).isEqualTo(now);
    }

    @Test
    void shouldUseSettersAndGetters() {
        // Given
        AccountInterestData data = new AccountInterestData();
        LocalDateTime now = LocalDateTime.now();

        // When
        data.setAccountId(1L);
        data.setAccountNumber("400012345678");
        data.setAccountType(Account.AccountType.CHECKING);
        data.setCustomerId(200L);
        data.setOriginalBalance(new BigDecimal("2000.00"));
        data.setCalculatedInterest(new BigDecimal("20.00"));
        data.setNewBalance(new BigDecimal("2020.00"));
        data.setCalculatedAt(now);

        // Then
        assertThat(data.getAccountId()).isEqualTo(1L);
        assertThat(data.getAccountNumber()).isEqualTo("400012345678");
        assertThat(data.getAccountType()).isEqualTo(Account.AccountType.CHECKING);
        assertThat(data.getCustomerId()).isEqualTo(200L);
        assertThat(data.getOriginalBalance()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(data.getCalculatedInterest()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(data.getNewBalance()).isEqualByComparingTo(new BigDecimal("2020.00"));
        assertThat(data.getCalculatedAt()).isEqualTo(now);
    }
}
