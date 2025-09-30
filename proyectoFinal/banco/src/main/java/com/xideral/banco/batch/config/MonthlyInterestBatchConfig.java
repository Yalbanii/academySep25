package com.xideral.banco.batch.config;

import com.xideral.banco.account.model.Account;
import com.xideral.banco.account.repository.AccountRepository;
import com.xideral.banco.batch.dto.AccountInterestData;
import com.xideral.banco.batch.interest.InterestCalculator;
import com.xideral.banco.batch.interest.InterestCalculatorFactory;
import com.xideral.banco.batch.listener.BatchJobExecutionMongoListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Configuración de Spring Batch para el procesamiento mensual de intereses.
 *
 * Job: monthlyInterestJob
 * Step 1: calculateInterestStep - Lee cuentas, calcula intereses
 * Step 2: applyInterestStep - Aplica intereses a las cuentas
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MonthlyInterestBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AccountRepository accountRepository;
    private final InterestCalculatorFactory calculatorFactory;
    private final BatchJobExecutionMongoListener batchJobExecutionMongoListener;

    // ========== JOB DEFINITION ==========

    @Bean
    public Job monthlyInterestJob() {
        return new JobBuilder("monthlyInterestJob", jobRepository)
                .listener(batchJobExecutionMongoListener)
                .start(calculateInterestStep())
                .build();
    }

    // ========== STEP 1: CALCULATE INTEREST ==========

    @Bean
    public Step calculateInterestStep() {
        return new StepBuilder("calculateInterestStep", jobRepository)
                .<Account, AccountInterestData>chunk(10, transactionManager)
                .reader(accountReader())
                .processor(interestCalculatorProcessor())
                .writer(interestApplierWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<Account> accountReader() {
        return new RepositoryItemReaderBuilder<Account>()
                .name("accountReader")
                .repository(accountRepository)
                .methodName("findByActive")
                .arguments(List.of(true))
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .pageSize(10)
                .build();
    }

    @Bean
    public ItemProcessor<Account, AccountInterestData> interestCalculatorProcessor() {
        return account -> {
            log.info("Processing account: {} (Type: {}, Balance: ${})",
                    account.getAccountNumber(),
                    account.getAccountType(),
                    account.getBalance());

            try {
                // POLIMORFISMO: Obtiene el calculador correcto según el tipo de cuenta
                InterestCalculator calculator = calculatorFactory.getCalculator(account.getAccountType());
                BigDecimal interest = calculator.calculateInterest(account);

                if (interest.compareTo(BigDecimal.ZERO) > 0) {
                    log.info("Interest calculated for account {}: ${} (Rate: {}%)",
                            account.getAccountNumber(),
                            interest,
                            calculator.getInterestRate().multiply(new BigDecimal("100")));

                    return new AccountInterestData(account, interest);
                } else {
                    log.debug("No interest calculated for account {} (balance: ${})",
                            account.getAccountNumber(),
                            account.getBalance());
                    return null; // Skip accounts with no interest
                }
            } catch (Exception e) {
                log.error("Error calculating interest for account {}: {}",
                        account.getAccountNumber(), e.getMessage());
                return null;
            }
        };
    }

    @Bean
    public ItemWriter<AccountInterestData> interestApplierWriter() {
        return items -> {
            for (AccountInterestData data : items) {
                if (data != null && data.shouldApplyInterest()) {
                    Account account = accountRepository.findById(data.getAccountId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Account not found: " + data.getAccountId()));

                    BigDecimal newBalance = account.getBalance().add(data.getCalculatedInterest());
                    account.setBalance(newBalance);
                    account.setUpdatedAt(LocalDateTime.now());

                    accountRepository.save(account);

                    log.info("✅ Interest applied to account {}: ${} (Old: ${}, New: ${})",
                            account.getAccountNumber(),
                            data.getCalculatedInterest(),
                            data.getOriginalBalance(),
                            newBalance);
                }
            }
        };
    }
}