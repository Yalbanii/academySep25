package com.xideral.banco.account.controller;

import com.xideral.banco.account.dto.AccountRequest;
import com.xideral.banco.account.dto.AccountResponse;
import com.xideral.banco.account.dto.TransactionRequest;
import com.xideral.banco.account.dto.TransferRequest;
import com.xideral.banco.account.model.Account;
import com.xideral.banco.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Account", description = "Account management and banking operations APIs")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Create a new account")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountRequest request) {
        Account account = new Account();
        account.setCustomerId(request.getCustomerId());
        account.setAccountType(request.getAccountType());
        account.setBalance(request.getInitialBalance());

        Account createdAccount = accountService.createAccount(account);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AccountResponse.fromEntity(createdAccount));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id) {
        Account account = accountService.getAccountById(id);
        return ResponseEntity.ok(AccountResponse.fromEntity(account));
    }

    @GetMapping("/number/{accountNumber}")
    @Operation(summary = "Get account by account number")
    public ResponseEntity<AccountResponse> getAccountByAccountNumber(@PathVariable String accountNumber) {
        Account account = accountService.getAccountByAccountNumber(accountNumber);
        return ResponseEntity.ok(AccountResponse.fromEntity(account));
    }

    @GetMapping
    @Operation(summary = "Get all accounts")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        List<AccountResponse> accounts = accountService.getAllAccounts()
                .stream()
                .map(AccountResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all accounts by customer ID")
    public ResponseEntity<List<AccountResponse>> getAccountsByCustomerId(@PathVariable Long customerId) {
        List<AccountResponse> accounts = accountService.getAccountsByCustomerId(customerId)
                .stream()
                .map(AccountResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/customer/{customerId}/active")
    @Operation(summary = "Get active accounts by customer ID")
    public ResponseEntity<List<AccountResponse>> getActiveAccountsByCustomerId(@PathVariable Long customerId) {
        List<AccountResponse> accounts = accountService.getActiveAccountsByCustomerId(customerId)
                .stream()
                .map(AccountResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get accounts by status")
    public ResponseEntity<List<AccountResponse>> getAccountsByStatus(@PathVariable Account.AccountStatus status) {
        List<AccountResponse> accounts = accountService.getAccountsByStatus(status)
                .stream()
                .map(AccountResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get accounts by type")
    public ResponseEntity<List<AccountResponse>> getAccountsByType(@PathVariable Account.AccountType type) {
        List<AccountResponse> accounts = accountService.getAccountsByType(type)
                .stream()
                .map(AccountResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update account")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountRequest request) {
        Account account = new Account();
        account.setAccountType(request.getAccountType());

        Account updatedAccount = accountService.updateAccount(id, account);
        return ResponseEntity.ok(AccountResponse.fromEntity(updatedAccount));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete account (soft delete)")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate account")
    public ResponseEntity<AccountResponse> activateAccount(@PathVariable Long id) {
        Account account = accountService.activateAccount(id);
        return ResponseEntity.ok(AccountResponse.fromEntity(account));
    }

    @PatchMapping("/{id}/close")
    @Operation(summary = "Close account")
    public ResponseEntity<AccountResponse> closeAccount(@PathVariable Long id) {
        Account account = accountService.closeAccount(id);
        return ResponseEntity.ok(AccountResponse.fromEntity(account));
    }

    // Banking Operations

    @PostMapping("/deposit")
    @Operation(summary = "Deposit money to account")
    public ResponseEntity<AccountResponse> deposit(@Valid @RequestBody TransactionRequest request) {
        Account account = accountService.deposit(request.getAccountNumber(), request.getAmount());
        return ResponseEntity.ok(AccountResponse.fromEntity(account));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw money from account")
    public ResponseEntity<AccountResponse> withdraw(@Valid @RequestBody TransactionRequest request) {
        Account account = accountService.withdraw(request.getAccountNumber(), request.getAmount());
        return ResponseEntity.ok(AccountResponse.fromEntity(account));
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money between accounts")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest request) {
        accountService.transfer(
                request.getFromAccountNumber(),
                request.getToAccountNumber(),
                request.getAmount()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/customer/{customerId}/count")
    @Operation(summary = "Count accounts by customer ID")
    public ResponseEntity<Long> countAccountsByCustomerId(@PathVariable Long customerId) {
        long count = accountService.countAccountsByCustomerId(customerId);
        return ResponseEntity.ok(count);
    }
}