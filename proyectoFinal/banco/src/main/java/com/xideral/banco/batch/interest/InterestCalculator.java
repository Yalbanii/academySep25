package com.xideral.banco.batch.interest;

import com.xideral.banco.account.model.Account;
import java.math.BigDecimal;

/**
 * Interface que define el contrato para el cálculo de intereses.
 * Implementa el patrón Strategy para permitir diferentes cálculos
 * según el tipo de cuenta (POLIMORFISMO).
 */
public interface InterestCalculator {

    /**
     * Calcula el interés para una cuenta específica.
     *
     * @param account Cuenta bancaria
     * @return Monto del interés calculado
     */
    BigDecimal calculateInterest(Account account);

    /**
     * Obtiene la tasa de interés aplicable.
     *
     * @return Tasa de interés (por ejemplo, 0.05 para 5%)
     */
    BigDecimal getInterestRate();

    /**
     * Obtiene el tipo de cuenta al que aplica este calculador.
     *
     * @return Tipo de cuenta
     */
    Account.AccountType getAccountType();
}