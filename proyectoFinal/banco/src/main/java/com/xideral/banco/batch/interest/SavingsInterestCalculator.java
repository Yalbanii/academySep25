package com.xideral.banco.batch.interest;

import com.xideral.banco.account.model.Account;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Implementación del cálculo de intereses para cuentas de AHORRO (SAVINGS).
 * Aplica una tasa de interés del 5% anual (0.42% mensual).
 *
 * POLIMORFISMO: Esta clase implementa la interfaz InterestCalculator
 * con comportamiento específico para cuentas de ahorro.
 */
@Component
public class SavingsInterestCalculator implements InterestCalculator {

    // Tasa anual del 5% dividida entre 12 meses = 0.004166...
    private static final BigDecimal MONTHLY_INTEREST_RATE = new BigDecimal("0.004166667");

    @Override
    public BigDecimal calculateInterest(Account account) {
        if (account.getAccountType() != Account.AccountType.SAVINGS) {
            throw new IllegalArgumentException(
                "SavingsInterestCalculator only applies to SAVINGS accounts"
            );
        }

        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            return BigDecimal.ZERO;
        }

        BigDecimal balance = account.getBalance();
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // Interés = Balance * Tasa Mensual
        return balance.multiply(MONTHLY_INTEREST_RATE)
                     .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getInterestRate() {
        return MONTHLY_INTEREST_RATE;
    }

    @Override
    public Account.AccountType getAccountType() {
        return Account.AccountType.SAVINGS;
    }
}