package com.xideral.banco.notification.service;

import com.xideral.banco.events.InterestAppliedEvent;
import com.xideral.banco.events.TransactionCompletedEvent;
import com.xideral.banco.events.TransferCompletedEvent;
import com.xideral.banco.notification.model.TransactionLog;
import com.xideral.banco.notification.repository.TransactionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(MongoTemplate.class)
public class TransactionLogServiceImpl implements TransactionLogService {

    private final TransactionLogRepository transactionLogRepository;

    @Override
    public TransactionLog createTransactionLog(TransactionLog transactionLog) {
        log.info("Creating transaction log for account: {}", transactionLog.getAccountNumber());

        if (transactionLog.getTimestamp() == null) {
            transactionLog.setTimestamp(LocalDateTime.now());
        }

        return transactionLogRepository.save(transactionLog);
    }

    @Override
    public TransactionLog getTransactionLogById(String id) {
        return transactionLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction log not found with id: " + id));
    }

    @Override
    public List<TransactionLog> getAllTransactionLogs() {
        return transactionLogRepository.findAll();
    }

    @Override
    public List<TransactionLog> getTransactionLogsByAccountNumber(String accountNumber) {
        return transactionLogRepository.findByAccountNumber(accountNumber);
    }

    @Override
    public List<TransactionLog> getTransactionLogsByTransactionType(String transactionType) {
        return transactionLogRepository.findByTransactionType(transactionType);
    }

    @Override
    public List<TransactionLog> getTransactionLogsByCustomerId(Long customerId) {
        return transactionLogRepository.findByCustomerId(customerId);
    }

    @Override
    public List<TransactionLog> getTransactionLogsByAccountNumberOrderedByDate(String accountNumber) {
        return transactionLogRepository.findByAccountNumberOrderByTimestampDesc(accountNumber);
    }

    @Override
    public List<TransactionLog> getTransactionLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return transactionLogRepository.findByTimestampBetween(start, end);
    }

    @Override
    public List<TransactionLog> getTransactionLogsByAccountNumberAndDateRange(String accountNumber, LocalDateTime start, LocalDateTime end) {
        return transactionLogRepository.findByAccountNumberAndTimestampBetween(accountNumber, start, end);
    }

    @Override
    public List<TransactionLog> getTransactionLogsByCustomerIdAndDateRange(Long customerId, LocalDateTime start, LocalDateTime end) {
        return transactionLogRepository.findByCustomerIdAndTimestampBetween(customerId, start, end);
    }

    @Override
    public long countByTransactionType(String transactionType) {
        return transactionLogRepository.countByTransactionType(transactionType);
    }

    @Override
    public long countByAccountNumber(String accountNumber) {
        return transactionLogRepository.countByAccountNumber(accountNumber);
    }

    @Override
    public void deleteTransactionLog(String id) {
        if (!transactionLogRepository.existsById(id)) {
            throw new IllegalArgumentException("Transaction log not found with id: " + id);
        }
        transactionLogRepository.deleteById(id);
        log.info("Deleted transaction log: {}", id);
    }

    // Event Listeners
    @ApplicationModuleListener
    public void handleTransactionCompleted(TransactionCompletedEvent event) {
        log.debug("Logging TransactionCompletedEvent for account: {}", event.getAccountNumber());

        TransactionLog log = TransactionLog.builder()
                .transactionId(event.getTransactionId())
                .accountNumber(event.getAccountNumber())
                .transactionType(event.getTransactionType())
                .amount(event.getAmount())
                .balanceAfter(event.getNewBalance())
                .timestamp(event.getTimestamp())
                .description(String.format("%s transaction of %s", event.getTransactionType(), event.getAmount()))
                .customerId(0L) // Placeholder - should be extracted from event
                .status("SUCCESS")
                .build();

        createTransactionLog(log);
    }

    @ApplicationModuleListener
    public void handleTransferCompleted(TransferCompletedEvent event) {
        log.debug("Logging TransferCompletedEvent from {} to {}", event.getSourceAccountNumber(), event.getTargetAccountNumber());

        // Log para cuenta origen
        TransactionLog sourceLog = TransactionLog.builder()
                .transactionId(event.getTransactionId())
                .accountNumber(event.getSourceAccountNumber())
                .transactionType("TRANSFER_SENT")
                .amount(event.getAmount().negate())
                .timestamp(event.getTimestamp())
                .description(String.format("Transfer sent to %s", event.getTargetAccountNumber()))
                .customerId(0L) // Placeholder
                .status("SUCCESS")
                .build();

        createTransactionLog(sourceLog);

        // Log para cuenta destino
        TransactionLog targetLog = TransactionLog.builder()
                .transactionId(event.getTransactionId())
                .accountNumber(event.getTargetAccountNumber())
                .transactionType("TRANSFER_RECEIVED")
                .amount(event.getAmount())
                .timestamp(event.getTimestamp())
                .description(String.format("Transfer received from %s", event.getSourceAccountNumber()))
                .customerId(0L) // Placeholder
                .status("SUCCESS")
                .build();

        createTransactionLog(targetLog);
    }

    @ApplicationModuleListener
    public void handleInterestApplied(InterestAppliedEvent event) {
        log.debug("Logging InterestAppliedEvent for account: {}", event.getAccountNumber());

        TransactionLog log = TransactionLog.builder()
                .transactionId(java.util.UUID.randomUUID().toString())
                .accountNumber(event.getAccountNumber())
                .transactionType("INTEREST_APPLIED")
                .amount(event.getInterestAmount())
                .balanceAfter(event.getNewBalance())
                .timestamp(event.getTimestamp())
                .description(String.format("Monthly interest applied: %s", event.getInterestAmount()))
                .customerId(0L) // Placeholder
                .status("SUCCESS")
                .build();

        createTransactionLog(log);
    }
}
