package com.xideral.banco.batch.config;

import com.xideral.banco.account.model.Account;
import com.xideral.banco.account.repository.AccountRepository;
import com.xideral.banco.batch.dto.AccountInterestData;
import com.xideral.banco.batch.interest.InterestCalculator;
import com.xideral.banco.batch.interest.InterestCalculatorFactory;
import com.xideral.banco.batch.listener.BatchJobExecutionMongoListener;
import com.xideral.banco.customer.model.Customer;
import com.xideral.banco.customer.repository.CustomerRepository;
import com.xideral.banco.events.InterestAppliedEvent;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Configuración de Spring Batch para el procesamiento mensual de intereses.
 *
 * Job: monthlyInterestJob
 * Step 1: calculateAndApplyInterestStep - Lee cuentas, calcula y aplica intereses
 * Step 2: publishEventsStep - Publica eventos para logs en MongoDB
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.batch.job.enabled", havingValue = "true", matchIfMissing = true)
public class MonthlyInterestBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final InterestCalculatorFactory calculatorFactory;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired(required = false)
    private BatchJobExecutionMongoListener batchJobExecutionMongoListener;

    // ========== JOB DEFINITION ==========

    @Bean
    public Job monthlyInterestJob() {
        JobBuilder jobBuilder = new JobBuilder("monthlyInterestJob", jobRepository);

        // Only add MongoDB listener if available
        if (batchJobExecutionMongoListener != null) {
            jobBuilder.listener(batchJobExecutionMongoListener);
        }

        return jobBuilder
                .start(calculateAndApplyInterestStep())
                .next(publishEventsStep())
                .build();
    }

    // ========== STEP 1: CALCULATE AND APPLY INTEREST ==========

    @Bean
    public Step calculateAndApplyInterestStep() {
        return new StepBuilder("calculateAndApplyInterestStep", jobRepository)
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
                .methodName("findActiveAccounts")
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

                    BigDecimal previousBalance = account.getBalance();
                    BigDecimal newBalance = previousBalance.add(data.getCalculatedInterest());
                    account.setBalance(newBalance);
                    account.setUpdatedAt(LocalDateTime.now());

                    accountRepository.save(account);

                    log.info("✅ Interest applied to account {}: ${} (Old: ${}, New: ${})",
                            account.getAccountNumber(),
                            data.getCalculatedInterest(),
                            data.getOriginalBalance(),
                            newBalance);

                    // Publicar evento para auditoría
                    Customer customer = customerRepository.findById(account.getCustomerId()).orElse(null);
                    if (customer != null) {
                        InterestAppliedEvent event = new InterestAppliedEvent(
                                account.getAccountNumber(),
                                account.getAccountType().toString(),
                                data.getCalculatedInterest(),
                                previousBalance,
                                newBalance,
                                customer.getEmail(),
                                LocalDateTime.now()
                        );
                        eventPublisher.publishEvent(event);
                        log.debug("InterestAppliedEvent published for account: {}", account.getAccountNumber());
                    }
                }
            }
        };
    }

    // ========== STEP 2: PUBLISH EVENTS FOR MONGO LOGS ==========

    @Bean
    public Step publishEventsStep() {
        return new StepBuilder("publishEventsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("✅ Step 2: Events published successfully. MongoDB logs created via event listeners.");
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}