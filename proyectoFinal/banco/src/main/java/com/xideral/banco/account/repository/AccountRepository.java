package com.xideral.banco.account.repository;

import com.xideral.banco.account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // Buscar cuenta por número de cuenta
    Optional<Account> findByAccountNumber(String accountNumber);

    // Verificar si existe un número de cuenta
    boolean existsByAccountNumber(String accountNumber);

    // Buscar cuentas por ID de cliente
    List<Account> findByCustomerId(Long customerId);

    // Buscar cuentas por estado
    List<Account> findByStatus(Account.AccountStatus status);

    // Buscar cuentas por tipo
    List<Account> findByAccountType(Account.AccountType accountType);

    // Buscar cuentas activas de un cliente
    @Query("SELECT a FROM Account a WHERE a.customerId = :customerId AND a.status = 'ACTIVE'")
    List<Account> findActiveAccountsByCustomerId(@Param("customerId") Long customerId);

    // Contar cuentas de un cliente
    long countByCustomerId(Long customerId);

    // Buscar cuentas con balance mayor a un monto
    @Query("SELECT a FROM Account a WHERE a.balance >= :minBalance")
    List<Account> findAccountsWithMinBalance(@Param("minBalance") java.math.BigDecimal minBalance);

    // Buscar cuentas activas (para batch processing)
    @Query("SELECT a FROM Account a WHERE a.status = 'ACTIVE'")
    org.springframework.data.domain.Page<Account> findActiveAccounts(org.springframework.data.domain.Pageable pageable);
}