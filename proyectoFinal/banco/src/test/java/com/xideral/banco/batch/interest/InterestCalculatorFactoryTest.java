package com.xideral.banco.batch.interest;

import com.xideral.banco.account.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InterestCalculatorFactoryTest {

    private InterestCalculatorFactory factory;
    private SavingsInterestCalculator savingsCalculator;
    private CheckingInterestCalculator checkingCalculator;

    @BeforeEach
    void setUp() {
        savingsCalculator = new SavingsInterestCalculator();
        checkingCalculator = new CheckingInterestCalculator();
        List<InterestCalculator> calculators = Arrays.asList(savingsCalculator, checkingCalculator);
        factory = new InterestCalculatorFactory(calculators);
    }

    @Test
    void shouldReturnSavingsCalculatorForSavingsAccountType() {
        // When
        InterestCalculator calculator = factory.getCalculator(Account.AccountType.SAVINGS);

        // Then
        assertThat(calculator).isInstanceOf(SavingsInterestCalculator.class);
        assertThat(calculator.getAccountType()).isEqualTo(Account.AccountType.SAVINGS);
    }

    @Test
    void shouldReturnCheckingCalculatorForCheckingAccountType() {
        // When
        InterestCalculator calculator = factory.getCalculator(Account.AccountType.CHECKING);

        // Then
        assertThat(calculator).isInstanceOf(CheckingInterestCalculator.class);
        assertThat(calculator.getAccountType()).isEqualTo(Account.AccountType.CHECKING);
    }

    @Test
    void shouldReturnAllCalculatorsAsMap() {
        // When
        Map<Account.AccountType, InterestCalculator> allCalculators = factory.getAllCalculators();

        // Then
        assertThat(allCalculators).hasSize(2);
        assertThat(allCalculators).containsKey(Account.AccountType.SAVINGS);
        assertThat(allCalculators).containsKey(Account.AccountType.CHECKING);
        assertThat(allCalculators.get(Account.AccountType.SAVINGS))
            .isInstanceOf(SavingsInterestCalculator.class);
        assertThat(allCalculators.get(Account.AccountType.CHECKING))
            .isInstanceOf(CheckingInterestCalculator.class);
    }

    @Test
    void shouldReturnSameInstanceOnMultipleCalls() {
        // When
        InterestCalculator calc1 = factory.getCalculator(Account.AccountType.SAVINGS);
        InterestCalculator calc2 = factory.getCalculator(Account.AccountType.SAVINGS);

        // Then
        assertThat(calc1).isSameAs(calc2);
    }
}
