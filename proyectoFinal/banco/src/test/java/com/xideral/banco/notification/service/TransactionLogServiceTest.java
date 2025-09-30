package com.xideral.banco.notification.service;

import com.xideral.banco.events.InterestAppliedEvent;
import com.xideral.banco.events.TransactionCompletedEvent;
import com.xideral.banco.events.TransferCompletedEvent;
import com.xideral.banco.notification.model.TransactionLog;
import com.xideral.banco.notification.repository.TransactionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionLogServiceTest {

    @Mock
    private TransactionLogRepository transactionLogRepository;

    @InjectMocks
    private TransactionLogServiceImpl transactionLogService;

    private TransactionLog testLog;

    @BeforeEach
    void setUp() {
        testLog = TransactionLog.builder()
                .id("log-123")
                .transactionId("txn-123")
                .accountNumber("400012345678")
                .transactionType("DEPOSIT")
                .amount(new BigDecimal("1000.00"))
                .balanceAfter(new BigDecimal("5000.00"))
                .timestamp(LocalDateTime.now())
                .description("Test deposit")
                .customerId(1L)
                .status("SUCCESS")
                .build();
    }

    // ========== CREATE LOG TESTS ==========

    @Test
    void createTransactionLog_Success() {
        // Arrange
        when(transactionLogRepository.save(any(TransactionLog.class))).thenReturn(testLog);

        // Act
        TransactionLog result = transactionLogService.createTransactionLog(testLog);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("log-123");
        verify(transactionLogRepository, times(1)).save(any(TransactionLog.class));
    }

    @Test
    void createTransactionLog_SetsDefaultTimestamp() {
        // Arrange
        TransactionLog logWithoutTimestamp = TransactionLog.builder()
                .transactionId("txn-124")
                .accountNumber("400012345678")
                .transactionType("WITHDRAWAL")
                .amount(new BigDecimal("500.00"))
                .customerId(1L)
                .status("SUCCESS")
                .build();
        when(transactionLogRepository.save(any(TransactionLog.class))).thenReturn(logWithoutTimestamp);

        // Act
        TransactionLog result = transactionLogService.createTransactionLog(logWithoutTimestamp);

        // Assert
        verify(transactionLogRepository, times(1)).save(any(TransactionLog.class));
    }

    // ========== GET LOG TESTS ==========

    @Test
    void getTransactionLogById_Success() {
        // Arrange
        when(transactionLogRepository.findById("log-123")).thenReturn(Optional.of(testLog));

        // Act
        TransactionLog result = transactionLogService.getTransactionLogById("log-123");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("log-123");
        verify(transactionLogRepository, times(1)).findById("log-123");
    }

    @Test
    void getTransactionLogById_NotFound() {
        // Arrange
        when(transactionLogRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> transactionLogService.getTransactionLogById("invalid-id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction log not found");
        verify(transactionLogRepository, times(1)).findById("invalid-id");
    }

    @Test
    void getAllTransactionLogs_Success() {
        // Arrange
        List<TransactionLog> logs = Arrays.asList(testLog);
        when(transactionLogRepository.findAll()).thenReturn(logs);

        // Act
        List<TransactionLog> result = transactionLogService.getAllTransactionLogs();

        // Assert
        assertThat(result).hasSize(1);
        verify(transactionLogRepository, times(1)).findAll();
    }

    // ========== QUERY TESTS ==========

    @Test
    void getTransactionLogsByAccountNumber_Success() {
        // Arrange
        List<TransactionLog> logs = Arrays.asList(testLog);
        when(transactionLogRepository.findByAccountNumber("400012345678")).thenReturn(logs);

        // Act
        List<TransactionLog> result = transactionLogService.getTransactionLogsByAccountNumber("400012345678");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAccountNumber()).isEqualTo("400012345678");
        verify(transactionLogRepository, times(1)).findByAccountNumber("400012345678");
    }

    @Test
    void getTransactionLogsByTransactionType_Success() {
        // Arrange
        List<TransactionLog> logs = Arrays.asList(testLog);
        when(transactionLogRepository.findByTransactionType("DEPOSIT")).thenReturn(logs);

        // Act
        List<TransactionLog> result = transactionLogService.getTransactionLogsByTransactionType("DEPOSIT");

        // Assert
        assertThat(result).hasSize(1);
        verify(transactionLogRepository, times(1)).findByTransactionType("DEPOSIT");
    }

    @Test
    void getTransactionLogsByCustomerId_Success() {
        // Arrange
        List<TransactionLog> logs = Arrays.asList(testLog);
        when(transactionLogRepository.findByCustomerId(1L)).thenReturn(logs);

        // Act
        List<TransactionLog> result = transactionLogService.getTransactionLogsByCustomerId(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerId()).isEqualTo(1L);
        verify(transactionLogRepository, times(1)).findByCustomerId(1L);
    }

    @Test
    void getTransactionLogsByAccountNumberOrderedByDate_Success() {
        // Arrange
        List<TransactionLog> logs = Arrays.asList(testLog);
        when(transactionLogRepository.findByAccountNumberOrderByTimestampDesc("400012345678"))
                .thenReturn(logs);

        // Act
        List<TransactionLog> result = transactionLogService
                .getTransactionLogsByAccountNumberOrderedByDate("400012345678");

        // Assert
        assertThat(result).hasSize(1);
        verify(transactionLogRepository, times(1))
                .findByAccountNumberOrderByTimestampDesc("400012345678");
    }

    @Test
    void getTransactionLogsByDateRange_Success() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<TransactionLog> logs = Arrays.asList(testLog);
        when(transactionLogRepository.findByTimestampBetween(start, end)).thenReturn(logs);

        // Act
        List<TransactionLog> result = transactionLogService.getTransactionLogsByDateRange(start, end);

        // Assert
        assertThat(result).hasSize(1);
        verify(transactionLogRepository, times(1)).findByTimestampBetween(start, end);
    }

    @Test
    void countByTransactionType_Success() {
        // Arrange
        when(transactionLogRepository.countByTransactionType("DEPOSIT")).thenReturn(5L);

        // Act
        long count = transactionLogService.countByTransactionType("DEPOSIT");

        // Assert
        assertThat(count).isEqualTo(5L);
        verify(transactionLogRepository, times(1)).countByTransactionType("DEPOSIT");
    }

    @Test
    void countByAccountNumber_Success() {
        // Arrange
        when(transactionLogRepository.countByAccountNumber("400012345678")).thenReturn(10L);

        // Act
        long count = transactionLogService.countByAccountNumber("400012345678");

        // Assert
        assertThat(count).isEqualTo(10L);
        verify(transactionLogRepository, times(1)).countByAccountNumber("400012345678");
    }

    // ========== DELETE LOG TESTS ==========

    @Test
    void deleteTransactionLog_Success() {
        // Arrange
        when(transactionLogRepository.existsById("log-123")).thenReturn(true);
        doNothing().when(transactionLogRepository).deleteById("log-123");

        // Act
        transactionLogService.deleteTransactionLog("log-123");

        // Assert
        verify(transactionLogRepository, times(1)).existsById("log-123");
        verify(transactionLogRepository, times(1)).deleteById("log-123");
    }

    @Test
    void deleteTransactionLog_NotFound() {
        // Arrange
        when(transactionLogRepository.existsById("invalid-id")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> transactionLogService.deleteTransactionLog("invalid-id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction log not found");
        verify(transactionLogRepository, times(1)).existsById("invalid-id");
        verify(transactionLogRepository, never()).deleteById(anyString());
    }

    // ========== EVENT LISTENER TESTS ==========

    @Test
    void handleTransactionCompleted_Success() {
        // Arrange
        TransactionCompletedEvent event = new TransactionCompletedEvent(
                "txn-123",
                "400012345678",
                "DEPOSIT",
                new BigDecimal("1000.00"),
                new BigDecimal("5000.00"),
                "test@example.com",
                LocalDateTime.now()
        );
        when(transactionLogRepository.save(any(TransactionLog.class))).thenReturn(testLog);

        // Act
        transactionLogService.handleTransactionCompleted(event);

        // Assert
        verify(transactionLogRepository, times(1)).save(any(TransactionLog.class));
    }

    @Test
    void handleTransferCompleted_Success() {
        // Arrange
        TransferCompletedEvent event = new TransferCompletedEvent(
                "txn-124",
                "400012345678",
                "400087654321",
                new BigDecimal("500.00"),
                "sender@example.com",
                "receiver@example.com",
                LocalDateTime.now()
        );
        when(transactionLogRepository.save(any(TransactionLog.class))).thenReturn(testLog);

        // Act
        transactionLogService.handleTransferCompleted(event);

        // Assert
        verify(transactionLogRepository, times(2)).save(any(TransactionLog.class)); // 2 logs: source + target
    }

    @Test
    void handleInterestApplied_Success() {
        // Arrange
        InterestAppliedEvent event = new InterestAppliedEvent(
                "400012345678",
                "SAVINGS",
                new BigDecimal("50.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("1050.00"),
                "test@example.com",
                LocalDateTime.now()
        );
        when(transactionLogRepository.save(any(TransactionLog.class))).thenReturn(testLog);

        // Act
        transactionLogService.handleInterestApplied(event);

        // Assert
        verify(transactionLogRepository, times(1)).save(any(TransactionLog.class));
    }
}
