package com.xideral.banco.batch.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller para ejecutar batch jobs manualmente.
 */
@Slf4j
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.batch.job.enabled", havingValue = "true", matchIfMissing = true)
public class BatchJobController {

    private final JobLauncher jobLauncher;
    private final Job monthlyInterestJob;

    @PostMapping("/monthly-interest")
    public ResponseEntity<Map<String, Object>> runMonthlyInterestJob() {
        log.info("Manual trigger: Monthly Interest Job");

        try {
            // Parámetros únicos para cada ejecución
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("timestamp", LocalDateTime.now().toString())
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(monthlyInterestJob, jobParameters);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Monthly Interest Job triggered successfully");
            response.put("timestamp", LocalDateTime.now());
            response.put("status", "RUNNING");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error executing Monthly Interest Job", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to execute batch job");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}