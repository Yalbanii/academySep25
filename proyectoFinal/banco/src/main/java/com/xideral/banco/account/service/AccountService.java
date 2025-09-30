package com.xideral.banco.account.service;

import com.xideral.banco.account.model.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    // CRUD operations
    Account createAccount(Account account);
    Account getAccountById(Long id);
    Account getAccountByAccountNumber(String accountNumber);
    List<Account> getAllAccounts();
    List<Account> getAccountsByCustomerId(Long customerId);
    List<Account> getActiveAccountsByCustomerId(Long customerId);
    Account updateAccount(Long id, Account account);
    void deleteAccount(Long id);

    // Account status operations
    Account activateAccount(Long id);
    Account closeAccount(Long id);

    // Banking operations
    Account deposit(String accountNumber, BigDecimal amount);
    Account withdraw(String accountNumber, BigDecimal amount);
    void transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount);

    // Query operations
    List<Account> getAccountsByStatus(Account.AccountStatus status);
    List<Account> getAccountsByType(Account.AccountType accountType);
    long countAccountsByCustomerId(Long customerId);
    boolean existsByAccountNumber(String accountNumber);
}