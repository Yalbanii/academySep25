package com.xideral.banco.batch.interest;

import com.xideral.banco.account.model.Account;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Implementación del cálculo de intereses para cuentas CORRIENTES (CHECKING).
 * Aplica una tasa de interés del 1% anual (0.083% mensual).
 *
 * POLIMORFISMO: Esta clase implementa la interfaz InterestCalculator
 * con comportamiento específico para cuentas corrientes.
 */
@Component
public class CheckingInterestCalculator implements InterestCalculator {

    // Tasa anual del 1% dividida entre 12 meses = 0.000833...
    private static final BigDecimal MONTHLY_INTEREST_RATE = new BigDecimal("0.000833333");

    @Override
    public BigDecimal calculateInterest(Account account) {
        if (account.getAccountType() != Account.AccountType.CHECKING) {
            throw new IllegalArgumentException(
                "CheckingInterestCalculator only applies to CHECKING accounts"
            );
        }

        if (!account.isActive()) {
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
        return Account.AccountType.CHECKING;
    }
}