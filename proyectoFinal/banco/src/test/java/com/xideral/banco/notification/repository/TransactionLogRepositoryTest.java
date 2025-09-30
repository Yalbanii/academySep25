package com.xideral.banco.notification.repository;

import com.xideral.banco.notification.model.TransactionLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
@EnabledIfSystemProperty(named = "mongodb.tests.enabled", matches = "true", disabledReason = "MongoDB not available")
class TransactionLogRepositoryTest {

    @Autowired
    private TransactionLogRepository transactionLogRepository;

    private TransactionLog testLog;

    @BeforeEach
    void setUp() {
        transactionLogRepository.deleteAll();

        testLog = TransactionLog.builder()
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

    @Test
    void save_Success() {
        // Act
        TransactionLog saved = transactionLogRepository.save(testLog);

        // Assert
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTransactionId()).isEqualTo("txn-123");
    }

    @Test
    void findByAccountNumber_Success() {
        // Arrange
        transactionLogRepository.save(testLog);

        // Act
        List<TransactionLog> result = transactionLogRepository.findByAccountNumber("400012345678");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAccountNumber()).isEqualTo("400012345678");
    }

    @Test
    void findByTransactionType_Success() {
        // Arrange
        transactionLogRepository.save(testLog);

        // Act
        List<TransactionLog> result = transactionLogRepository.findByTransactionType("DEPOSIT");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTransactionType()).isEqualTo("DEPOSIT");
    }

    @Test
    void findByCustomerId_Success() {
        // Arrange
        transactionLogRepository.save(testLog);

        // Act
        List<TransactionLog> result = transactionLogRepository.findByCustomerId(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerId()).isEqualTo(1L);
    }

    @Test
    void findByAccountNumberOrderByTimestampDesc_Success() {
        // Arrange
        TransactionLog log1 = testLog;
        log1.setTimestamp(LocalDateTime.now().minusDays(1));
        transactionLogRepository.save(log1);

        TransactionLog log2 = TransactionLog.builder()
                .transactionId("txn-124")
                .accountNumber("400012345678")
                .transactionType("WITHDRAWAL")
                .amount(new BigDecimal("500.00"))
                .balanceAfter(new BigDecimal("4500.00"))
                .timestamp(LocalDateTime.now())
                .description("Test withdrawal")
                .customerId(1L)
                .status("SUCCESS")
                .build();
        transactionLogRepository.save(log2);

        // Act
        List<TransactionLog> result = transactionLogRepository.findByAccountNumberOrderByTimestampDesc("400012345678");

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTransactionId()).isEqualTo("txn-124"); // Most recent first
        assertThat(result.get(1).getTransactionId()).isEqualTo("txn-123");
    }

    @Test
    void findByTimestampBetween_Success() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        testLog.setTimestamp(now);
        transactionLogRepository.save(testLog);

        // Act
        List<TransactionLog> result = transactionLogRepository.findByTimestampBetween(
                now.minusHours(1),
                now.plusHours(1)
        );

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void findByAccountNumberAndTimestampBetween_Success() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        testLog.setTimestamp(now);
        transactionLogRepository.save(testLog);

        // Act
        List<TransactionLog> result = transactionLogRepository.findByAccountNumberAndTimestampBetween(
                "400012345678",
                now.minusHours(1),
                now.plusHours(1)
        );

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAccountNumber()).isEqualTo("400012345678");
    }

    @Test
    void findByCustomerIdAndTimestampBetween_Success() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        testLog.setTimestamp(now);
        transactionLogRepository.save(testLog);

        // Act
        List<TransactionLog> result = transactionLogRepository.findByCustomerIdAndTimestampBetween(
                1L,
                now.minusHours(1),
                now.plusHours(1)
        );

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerId()).isEqualTo(1L);
    }

    @Test
    void countByTransactionType_Success() {
        // Arrange
        transactionLogRepository.save(testLog);

        TransactionLog log2 = TransactionLog.builder()
                .transactionId("txn-125")
                .accountNumber("400087654321")
                .transactionType("DEPOSIT")
                .amount(new BigDecimal("2000.00"))
                .timestamp(LocalDateTime.now())
                .customerId(2L)
                .status("SUCCESS")
                .build();
        transactionLogRepository.save(log2);

        // Act
        long count = transactionLogRepository.countByTransactionType("DEPOSIT");

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    void countByAccountNumber_Success() {
        // Arrange
        transactionLogRepository.save(testLog);

        TransactionLog log2 = TransactionLog.builder()
                .transactionId("txn-126")
                .accountNumber("400012345678")
                .transactionType("WITHDRAWAL")
                .amount(new BigDecimal("500.00"))
                .timestamp(LocalDateTime.now())
                .customerId(1L)
                .status("SUCCESS")
                .build();
        transactionLogRepository.save(log2);

        // Act
        long count = transactionLogRepository.countByAccountNumber("400012345678");

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    void deleteAll_Success() {
        // Arrange
        transactionLogRepository.save(testLog);

        // Act
        transactionLogRepository.deleteAll();
        List<TransactionLog> result = transactionLogRepository.findAll();

        // Assert
        assertThat(result).isEmpty();
    }
}
