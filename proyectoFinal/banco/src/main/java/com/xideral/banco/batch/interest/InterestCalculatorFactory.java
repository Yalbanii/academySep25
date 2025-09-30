package com.xideral.banco.batch.interest;

import com.xideral.banco.account.model.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory que selecciona el calculador de intereses apropiado
 * según el tipo de cuenta.
 *
 * POLIMORFISMO: Utiliza un Map para seleccionar dinámicamente
 * la implementación correcta basándose en el tipo de cuenta.
 */
@Component
@RequiredArgsConstructor
public class InterestCalculatorFactory {

    private final List<InterestCalculator> calculators;

    /**
     * Obtiene el calculador apropiado para el tipo de cuenta.
     *
     * @param accountType Tipo de cuenta
     * @return Calculador de intereses correspondiente
     * @throws IllegalArgumentException si no existe calculador para el tipo
     */
    public InterestCalculator getCalculator(Account.AccountType accountType) {
        return calculators.stream()
                .filter(calc -> calc.getAccountType() == accountType)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "No interest calculator found for account type: " + accountType
                ));
    }

    /**
     * Obtiene todos los calculadores disponibles como un Map.
     *
     * @return Map de tipo de cuenta a calculador
     */
    public Map<Account.AccountType, InterestCalculator> getAllCalculators() {
        return calculators.stream()
                .collect(Collectors.toMap(
                    InterestCalculator::getAccountType,
                    Function.identity()
                ));
    }
}