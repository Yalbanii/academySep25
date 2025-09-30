package com.xideral.banco.notification.service;

import com.xideral.banco.notification.model.TransactionLog;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionLogService {

    TransactionLog createTransactionLog(TransactionLog transactionLog);

    TransactionLog getTransactionLogById(String id);

    List<TransactionLog> getAllTransactionLogs();

    List<TransactionLog> getTransactionLogsByAccountNumber(String accountNumber);

    List<TransactionLog> getTransactionLogsByTransactionType(String transactionType);

    List<TransactionLog> getTransactionLogsByCustomerId(Long customerId);

    List<TransactionLog> getTransactionLogsByStatus(String status);

    List<TransactionLog> getTransactionLogsByAccountNumberOrderedByDate(String accountNumber);

    List<TransactionLog> getTransactionLogsByDateRange(LocalDateTime start, LocalDateTime end);

    List<TransactionLog> getTransactionLogsByAccountNumberAndDateRange(String accountNumber, LocalDateTime start, LocalDateTime end);

    List<TransactionLog> getTransactionLogsByCustomerIdAndDateRange(Long customerId, LocalDateTime start, LocalDateTime end);

    List<TransactionLog> getTransactionLogsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount);

    long countByTransactionType(String transactionType);

    long countByAccountNumber(String accountNumber);

    long countByStatus(String status);

    void deleteTransactionLog(String id);
}
