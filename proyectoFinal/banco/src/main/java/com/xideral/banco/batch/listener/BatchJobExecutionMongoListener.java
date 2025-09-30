package com.xideral.banco.batch.listener;

import com.xideral.banco.batch.model.BatchJobExecutionLog;
import com.xideral.banco.batch.repository.BatchJobExecutionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Listener que registra la ejecución de batch jobs en MongoDB.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.batch.job.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(name = "mongoTemplate")
public class BatchJobExecutionMongoListener implements JobExecutionListener {

    private final BatchJobExecutionLogRepository logRepository;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("🚀 Starting batch job: {} (ID: {})",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getId());

        BatchJobExecutionLog executionLog = BatchJobExecutionLog.started(
                jobExecution.getId(),
                jobExecution.getJobInstance().getJobName()
        );

        logRepository.save(executionLog);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        BatchJobExecutionLog log = logRepository.findByJobExecutionId(jobExecution.getId());

        if (log == null) {
            this.log.warn("No execution log found for job execution: {}", jobExecution.getId());
            return;
        }

        if (jobExecution.getStatus().isUnsuccessful()) {
            String errorMessage = jobExecution.getAllFailureExceptions().stream()
                    .map(Throwable::getMessage)
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Unknown error");

            log.failed(errorMessage);
            this.log.error("❌ Batch job failed: {} - {}", jobExecution.getJobInstance().getJobName(), errorMessage);
        } else {
            // Get statistics from job context
            Integer totalAccounts = (Integer) jobExecution.getExecutionContext().get("totalAccounts");
            Integer accountsWithInterest = (Integer) jobExecution.getExecutionContext().get("accountsWithInterest");
            String totalInterest = (String) jobExecution.getExecutionContext().get("totalInterest");

            log.completed(
                    totalAccounts != null ? totalAccounts : 0,
                    accountsWithInterest != null ? accountsWithInterest : 0,
                    totalInterest != null ? totalInterest : "0.00"
            );

            this.log.info("✅ Batch job completed: {} (Duration: {}ms, Accounts: {}, Interest Applied: ${})",
                    jobExecution.getJobInstance().getJobName(),
                    log.getDuration(),
                    totalAccounts,
                    totalInterest);
        }

        logRepository.save(log);
    }
}