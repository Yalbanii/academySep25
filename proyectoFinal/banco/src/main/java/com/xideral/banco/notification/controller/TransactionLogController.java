package com.xideral.banco.notification.controller;

import com.xideral.banco.notification.model.TransactionLog;
import com.xideral.banco.notification.service.TransactionLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Transaction Logs", description = "API para gestionar logs de transacciones en MongoDB")
@RestController
@RequestMapping("/api/transaction-logs")
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("!test")
public class TransactionLogController {

    private final TransactionLogService transactionLogService;

    @Operation(summary = "Obtener todos los transaction logs")
    @GetMapping
    public ResponseEntity<List<TransactionLog>> getAllTransactionLogs() {
        return ResponseEntity.ok(transactionLogService.getAllTransactionLogs());
    }

    @Operation(summary = "Obtener transaction log por ID")
    @GetMapping("/{id}")
    public ResponseEntity<TransactionLog> getTransactionLogById(@PathVariable String id) {
        return ResponseEntity.ok(transactionLogService.getTransactionLogById(id));
    }

    @Operation(summary = "Obtener transaction logs por número de cuenta")
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<TransactionLog>> getTransactionLogsByAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(transactionLogService.getTransactionLogsByAccountNumber(accountNumber));
    }

    @Operation(summary = "Obtener transaction logs por número de cuenta ordenados por fecha")
    @GetMapping("/account/{accountNumber}/ordered")
    public ResponseEntity<List<TransactionLog>> getTransactionLogsByAccountOrdered(@PathVariable String accountNumber) {
        return ResponseEntity.ok(transactionLogService.getTransactionLogsByAccountNumberOrderedByDate(accountNumber));
    }

    @Operation(summary = "Obtener transaction logs por tipo de transacción")
    @GetMapping("/transaction-type/{transactionType}")
    public ResponseEntity<List<TransactionLog>> getTransactionLogsByType(@PathVariable String transactionType) {
        return ResponseEntity.ok(transactionLogService.getTransactionLogsByTransactionType(transactionType));
    }

    @Operation(summary = "Obtener transaction logs por cliente")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<TransactionLog>> getTransactionLogsByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(transactionLogService.getTransactionLogsByCustomerId(customerId));
    }

    @Operation(summary = "Obtener transaction logs por estado")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TransactionLog>> getTransactionLogsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(transactionLogService.getTransactionLogsByStatus(status));
    }

    @Operation(summary = "Obtener transaction logs por rango de fechas")
    @GetMapping("/date-range")
    public ResponseEntity<List<TransactionLog>> getTransactionLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(transactionLogService.getTransactionLogsByDateRange(startDate, endDate));
    }

    @Operation(summary = "Obtener transaction logs por cuenta y rango de fechas")
    @GetMapping("/account/{accountNumber}/date-range")
    public ResponseEntity<List<TransactionLog>> getTransactionLogsByAccountAndDateRange(
            @PathVariable String accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(transactionLogService.getTransactionLogsByAccountNumberAndDateRange(accountNumber, startDate, endDate));
    }

    @Operation(summary = "Obtener transaction logs por rango de montos")
    @GetMapping("/amount-range")
    public ResponseEntity<List<TransactionLog>> getTransactionLogsByAmountRange(
            @RequestParam BigDecimal minAmount,
            @RequestParam BigDecimal maxAmount) {
        return ResponseEntity.ok(transactionLogService.getTransactionLogsByAmountRange(minAmount, maxAmount));
    }

    @Operation(summary = "Contar transaction logs por tipo de transacción")
    @GetMapping("/count/transaction-type/{transactionType}")
    public ResponseEntity<Long> countByTransactionType(@PathVariable String transactionType) {
        return ResponseEntity.ok(transactionLogService.countByTransactionType(transactionType));
    }

    @Operation(summary = "Contar transaction logs por estado")
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> countByStatus(@PathVariable String status) {
        return ResponseEntity.ok(transactionLogService.countByStatus(status));
    }

    @Operation(summary = "Contar transaction logs por cuenta")
    @GetMapping("/count/account/{accountNumber}")
    public ResponseEntity<Long> countByAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(transactionLogService.countByAccountNumber(accountNumber));
    }

    @Operation(summary = "Crear transaction log manual")
    @PostMapping
    public ResponseEntity<TransactionLog> createTransactionLog(@RequestBody TransactionLog transactionLog) {
        return ResponseEntity.ok(transactionLogService.createTransactionLog(transactionLog));
    }

    @Operation(summary = "Eliminar transaction log")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransactionLog(@PathVariable String id) {
        transactionLogService.deleteTransactionLog(id);
        return ResponseEntity.noContent().build();
    }
}
