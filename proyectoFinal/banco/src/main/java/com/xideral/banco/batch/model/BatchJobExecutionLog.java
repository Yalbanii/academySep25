package com.xideral.banco.batch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Modelo para almacenar los logs de ejecución de batch jobs en MongoDB.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "batch_job_executions")
public class BatchJobExecutionLog {

    @Id
    private String id;

    private Long jobExecutionId;
    private String jobName;
    private String status; // STARTED, COMPLETED, FAILED
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration; // milliseconds

    // Estadísticas del job
    private Integer totalAccountsProcessed;
    private Integer accountsWithInterest;
    private String totalInterestApplied;
    private String errorMessage;

    public static BatchJobExecutionLog started(Long jobExecutionId, String jobName) {
        BatchJobExecutionLog log = new BatchJobExecutionLog();
        log.setJobExecutionId(jobExecutionId);
        log.setJobName(jobName);
        log.setStatus("STARTED");
        log.setStartTime(LocalDateTime.now());
        return log;
    }

    public void completed(Integer totalAccounts, Integer accountsWithInterest, String totalInterest) {
        this.setStatus("COMPLETED");
        this.setEndTime(LocalDateTime.now());
        this.setDuration(java.time.Duration.between(startTime, endTime).toMillis());
        this.setTotalAccountsProcessed(totalAccounts);
        this.setAccountsWithInterest(accountsWithInterest);
        this.setTotalInterestApplied(totalInterest);
    }

    public void failed(String errorMessage) {
        this.setStatus("FAILED");
        this.setEndTime(LocalDateTime.now());
        this.setDuration(java.time.Duration.between(startTime, endTime).toMillis());
        this.setErrorMessage(errorMessage);
    }
}