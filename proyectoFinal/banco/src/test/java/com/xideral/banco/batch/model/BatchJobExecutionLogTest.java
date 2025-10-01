package com.xideral.banco.batch.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BatchJobExecutionLogTest {

    @Test
    void shouldCreateStartedLog() {
        // When
        BatchJobExecutionLog log = BatchJobExecutionLog.started(123L, "monthlyInterestJob");

        // Then
        assertThat(log.getJobExecutionId()).isEqualTo(123L);
        assertThat(log.getJobName()).isEqualTo("monthlyInterestJob");
        assertThat(log.getStatus()).isEqualTo("STARTED");
        assertThat(log.getStartTime()).isNotNull();
        assertThat(log.getEndTime()).isNull();
    }

    @Test
    void shouldMarkLogAsCompleted() throws InterruptedException {
        // Given
        BatchJobExecutionLog log = BatchJobExecutionLog.started(123L, "monthlyInterestJob");
        Thread.sleep(10); // Small delay to ensure duration > 0

        // When
        log.completed(100, 95, "1250.50");

        // Then
        assertThat(log.getStatus()).isEqualTo("COMPLETED");
        assertThat(log.getEndTime()).isNotNull();
        assertThat(log.getDuration()).isGreaterThan(0L);
        assertThat(log.getTotalAccountsProcessed()).isEqualTo(100);
        assertThat(log.getAccountsWithInterest()).isEqualTo(95);
        assertThat(log.getTotalInterestApplied()).isEqualTo("1250.50");
    }

    @Test
    void shouldMarkLogAsFailed() throws InterruptedException {
        // Given
        BatchJobExecutionLog log = BatchJobExecutionLog.started(123L, "monthlyInterestJob");
        Thread.sleep(10); // Small delay to ensure duration > 0

        // When
        log.failed("Database connection error");

        // Then
        assertThat(log.getStatus()).isEqualTo("FAILED");
        assertThat(log.getEndTime()).isNotNull();
        assertThat(log.getDuration()).isGreaterThan(0L);
        assertThat(log.getErrorMessage()).isEqualTo("Database connection error");
    }

    @Test
    void shouldCreateWithNoArgsConstructor() {
        // When
        BatchJobExecutionLog log = new BatchJobExecutionLog();

        // Then
        assertThat(log).isNotNull();
    }

    @Test
    void shouldCreateWithAllArgsConstructor() {
        // Given
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusMinutes(5);

        // When
        BatchJobExecutionLog log = new BatchJobExecutionLog(
            "log-id-123",
            456L,
            "testJob",
            "COMPLETED",
            startTime,
            endTime,
            300000L,
            50,
            48,
            "500.00",
            null
        );

        // Then
        assertThat(log.getId()).isEqualTo("log-id-123");
        assertThat(log.getJobExecutionId()).isEqualTo(456L);
        assertThat(log.getJobName()).isEqualTo("testJob");
        assertThat(log.getStatus()).isEqualTo("COMPLETED");
        assertThat(log.getStartTime()).isEqualTo(startTime);
        assertThat(log.getEndTime()).isEqualTo(endTime);
        assertThat(log.getDuration()).isEqualTo(300000L);
        assertThat(log.getTotalAccountsProcessed()).isEqualTo(50);
        assertThat(log.getAccountsWithInterest()).isEqualTo(48);
        assertThat(log.getTotalInterestApplied()).isEqualTo("500.00");
        assertThat(log.getErrorMessage()).isNull();
    }

    @Test
    void shouldUseSettersAndGetters() {
        // Given
        BatchJobExecutionLog log = new BatchJobExecutionLog();
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusMinutes(3);

        // When
        log.setId("test-id");
        log.setJobExecutionId(789L);
        log.setJobName("interestJob");
        log.setStatus("RUNNING");
        log.setStartTime(startTime);
        log.setEndTime(endTime);
        log.setDuration(180000L);
        log.setTotalAccountsProcessed(200);
        log.setAccountsWithInterest(190);
        log.setTotalInterestApplied("2500.75");
        log.setErrorMessage("Some warning");

        // Then
        assertThat(log.getId()).isEqualTo("test-id");
        assertThat(log.getJobExecutionId()).isEqualTo(789L);
        assertThat(log.getJobName()).isEqualTo("interestJob");
        assertThat(log.getStatus()).isEqualTo("RUNNING");
        assertThat(log.getStartTime()).isEqualTo(startTime);
        assertThat(log.getEndTime()).isEqualTo(endTime);
        assertThat(log.getDuration()).isEqualTo(180000L);
        assertThat(log.getTotalAccountsProcessed()).isEqualTo(200);
        assertThat(log.getAccountsWithInterest()).isEqualTo(190);
        assertThat(log.getTotalInterestApplied()).isEqualTo("2500.75");
        assertThat(log.getErrorMessage()).isEqualTo("Some warning");
    }
}
