package com.xideral.banco.account.service;

import com.xideral.banco.account.model.Account;
import com.xideral.banco.account.repository.AccountRepository;
import com.xideral.banco.customer.model.Customer;
import com.xideral.banco.customer.repository.CustomerRepository;
import com.xideral.banco.events.AccountCreatedEvent;
import com.xideral.banco.events.TransactionCompletedEvent;
import com.xideral.banco.events.TransferCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Random random = new Random();

    @Override
    public Account createAccount(Account account) {
        log.debug("Creating account for customer: {}", account.getCustomerId());

        // Validar que el cliente existe
        Customer customer = customerRepository.findById(account.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + account.getCustomerId()));

        // Validar que el cliente esté activo
        if (customer.getStatus() != Customer.CustomerStatus.ACTIVE) {
            throw new IllegalArgumentException("Customer is not active");
        }

        // Generar número de cuenta único
        String accountNumber = generateAccountNumber();
        account.setAccountNumber(accountNumber);

        // Establecer estado inicial
        account.setStatus(Account.AccountStatus.ACTIVE);

        // Balance inicial (0 por defecto)
        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }

        // Validar balance inicial no negativo
        if (account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }

        Account savedAccount = accountRepository.save(account);
        log.info("Account created successfully: {}", savedAccount.getAccountNumber());

        // Publicar evento
        AccountCreatedEvent event = new AccountCreatedEvent(
                savedAccount.getId(),
                savedAccount.getAccountNumber(),
                savedAccount.getAccountType().toString(),
                savedAccount.getBalance(),
                customer.getId(),
                customer.getEmail(),
                LocalDateTime.now()
        );
        eventPublisher.publishEvent(event);
        log.debug("AccountCreatedEvent published for account: {}", savedAccount.getAccountNumber());

        return savedAccount;
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccountById(Long id) {
        log.debug("Getting account by id: {}", id);
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccountByAccountNumber(String accountNumber) {
        log.debug("Getting account by account number: {}", accountNumber);
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with number: " + accountNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        log.debug("Getting all accounts");
        return accountRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAccountsByCustomerId(Long customerId) {
        log.debug("Getting accounts for customer: {}", customerId);
        return accountRepository.findByCustomerId(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getActiveAccountsByCustomerId(Long customerId) {
        log.debug("Getting active accounts for customer: {}", customerId);
        return accountRepository.findActiveAccountsByCustomerId(customerId);
    }

    @Override
    public Account updateAccount(Long id, Account account) {
        log.debug("Updating account with id: {}", id);

        Account existingAccount = getAccountById(id);

        // Solo permitir actualizar ciertos campos
        existingAccount.setAccountType(account.getAccountType());

        Account updatedAccount = accountRepository.save(existingAccount);
        log.info("Account updated successfully: {}", id);
        return updatedAccount;
    }

    @Override
    public void deleteAccount(Long id) {
        log.debug("Deleting account with id: {}", id);

        Account account = getAccountById(id);

        // Validar que el balance sea 0
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("Cannot delete account with non-zero balance");
        }

        // Soft delete: cambiar estado a CLOSED
        account.setStatus(Account.AccountStatus.CLOSED);
        accountRepository.save(account);

        log.info("Account soft deleted (closed): {}", id);
    }

    @Override
    public Account activateAccount(Long id) {
        log.debug("Activating account with id: {}", id);

        Account account = getAccountById(id);
        account.setStatus(Account.AccountStatus.ACTIVE);

        Account activatedAccount = accountRepository.save(account);
        log.info("Account activated: {}", id);
        return activatedAccount;
    }

    @Override
    public Account closeAccount(Long id) {
        log.debug("Closing account with id: {}", id);

        Account account = getAccountById(id);

        // Validar que el balance sea 0
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("Cannot close account with non-zero balance. Current balance: " + account.getBalance());
        }

        account.setStatus(Account.AccountStatus.CLOSED);

        Account closedAccount = accountRepository.save(account);
        log.info("Account closed: {}", id);

        return closedAccount;
    }

    @Override
    public Account deposit(String accountNumber, BigDecimal amount) {
        log.debug("Depositing {} to account: {}", amount, accountNumber);

        // Validaciones
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }

        Account account = getAccountByAccountNumber(accountNumber);

        // Validar que la cuenta esté activa
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Account is not active");
        }

        // Realizar depósito
        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);

        Account updatedAccount = accountRepository.save(account);
        log.info("Deposit successful. Account: {}, Amount: {}, New Balance: {}",
                accountNumber, amount, newBalance);

        // Publicar evento
        Customer customer = customerRepository.findById(account.getCustomerId()).orElse(null);
        if (customer != null) {
            TransactionCompletedEvent event = new TransactionCompletedEvent(
                    UUID.randomUUID().toString(),
                    accountNumber,
                    "DEPOSIT",
                    amount,
                    newBalance,
                    customer.getEmail(),
                    LocalDateTime.now()
            );
            eventPublisher.publishEvent(event);
            log.debug("TransactionCompletedEvent published for deposit on account: {}", accountNumber);
        }

        return updatedAccount;
    }

    @Override
    public Account withdraw(String accountNumber, BigDecimal amount) {
        log.debug("Withdrawing {} from account: {}", amount, accountNumber);

        // Validaciones
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero");
        }

        Account account = getAccountByAccountNumber(accountNumber);

        // Validar que la cuenta esté activa
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Account is not active");
        }

        // Validar fondos suficientes (con polimorfismo para tipos de cuenta)
        BigDecimal minimumBalance = getMinimumBalance(account);
        BigDecimal newBalance = account.getBalance().subtract(amount);

        if (newBalance.compareTo(minimumBalance) < 0) {
            throw new IllegalArgumentException(
                    String.format("Insufficient funds. Current balance: %s, Withdrawal: %s, Minimum allowed: %s",
                            account.getBalance(), amount, minimumBalance));
        }

        // Realizar retiro
        account.setBalance(newBalance);

        Account updatedAccount = accountRepository.save(account);
        log.info("Withdrawal successful. Account: {}, Amount: {}, New Balance: {}",
                accountNumber, amount, newBalance);

        // Publicar evento
        Customer customer = customerRepository.findById(account.getCustomerId()).orElse(null);
        if (customer != null) {
            TransactionCompletedEvent event = new TransactionCompletedEvent(
                    UUID.randomUUID().toString(),
                    accountNumber,
                    "WITHDRAWAL",
                    amount,
                    newBalance,
                    customer.getEmail(),
                    LocalDateTime.now()
            );
            eventPublisher.publishEvent(event);
            log.debug("TransactionCompletedEvent published for withdrawal on account: {}", accountNumber);
        }

        return updatedAccount;
    }

    @Override
    public void transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        log.debug("Transferring {} from {} to {}", amount, fromAccountNumber, toAccountNumber);

        // Validaciones
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }

        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        // Obtener cuentas
        Account fromAccount = getAccountByAccountNumber(fromAccountNumber);
        Account toAccount = getAccountByAccountNumber(toAccountNumber);

        // Validar que ambas cuentas estén activas
        if (fromAccount.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Source account is not active");
        }
        if (toAccount.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Destination account is not active");
        }

        // Validar fondos suficientes
        BigDecimal minimumBalance = getMinimumBalance(fromAccount);
        BigDecimal newFromBalance = fromAccount.getBalance().subtract(amount);

        if (newFromBalance.compareTo(minimumBalance) < 0) {
            throw new IllegalArgumentException("Insufficient funds for transfer");
        }

        // Realizar transferencia
        fromAccount.setBalance(newFromBalance);
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        log.info("Transfer successful. From: {}, To: {}, Amount: {}",
                fromAccountNumber, toAccountNumber, amount);

        // Publicar evento
        Customer fromCustomer = customerRepository.findById(fromAccount.getCustomerId()).orElse(null);
        Customer toCustomer = customerRepository.findById(toAccount.getCustomerId()).orElse(null);

        if (fromCustomer != null && toCustomer != null) {
            TransferCompletedEvent event = new TransferCompletedEvent(
                    UUID.randomUUID().toString(),
                    fromAccountNumber,
                    toAccountNumber,
                    amount,
                    fromCustomer.getEmail(),
                    toCustomer.getEmail(),
                    LocalDateTime.now()
            );
            eventPublisher.publishEvent(event);
            log.debug("TransferCompletedEvent published for transfer from {} to {}",
                    fromAccountNumber, toAccountNumber);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAccountsByStatus(Account.AccountStatus status) {
        log.debug("Getting accounts by status: {}", status);
        return accountRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAccountsByType(Account.AccountType accountType) {
        log.debug("Getting accounts by type: {}", accountType);
        return accountRepository.findByAccountType(accountType);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAccountsByCustomerId(Long customerId) {
        return accountRepository.countByCustomerId(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByAccountNumber(String accountNumber) {
        return accountRepository.existsByAccountNumber(accountNumber);
    }

    // Método privado para generar número de cuenta único
    private String generateAccountNumber() {
        String accountNumber;
        do {
            // Formato: 4 dígitos + 8 dígitos aleatorios (total 12 dígitos)
            accountNumber = String.format("4000%08d", random.nextInt(100000000));
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    // POLIMORFISMO: Obtener balance mínimo según tipo de cuenta
    private BigDecimal getMinimumBalance(Account account) {
        return switch (account.getAccountType()) {
            case CHECKING -> BigDecimal.ZERO; // Cuenta de cheques: puede llegar a 0
            case SAVINGS -> new BigDecimal("100.00"); // Cuenta de ahorros: mínimo $100
        };
    }
}