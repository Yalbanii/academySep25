package com.xideral.banco.batch.config;

import com.xideral.banco.account.model.Account;
import com.xideral.banco.account.repository.AccountRepository;
import com.xideral.banco.customer.model.Customer;
import com.xideral.banco.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class,
        MongoRepositoriesAutoConfiguration.class
})
@TestPropertySource(properties = {
        "spring.batch.job.enabled=true",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class MonthlyInterestBatchConfigTest {
/*
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        jobRepositoryTestUtils.removeJobExecutions();
        accountRepository.deleteAll();
        customerRepository.deleteAll();

        // Create test customer
        testCustomer = new Customer();
        testCustomer.setName("Test Customer");
        testCustomer.setEmail("test@example.com");
        testCustomer.setPhone("1234567890");
        testCustomer.setStatus(Customer.CustomerStatus.ACTIVE);
        testCustomer = customerRepository.save(testCustomer);
    }

    @Test
    void monthlyInterestJob_CompletesSuccessfully() throws Exception {
        // Arrange: Create test accounts
        Account savingsAccount = createAccount(Account.AccountType.SAVINGS, new BigDecimal("1000.00"));
        Account checkingAccount = createAccount(Account.AccountType.CHECKING, new BigDecimal("5000.00"));

        // Act: Launch the job
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Assert: Job completed successfully
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        // Verify that there are 2 steps
        List<StepExecution> stepExecutions = (List<StepExecution>) jobExecution.getStepExecutions();
        assertThat(stepExecutions).hasSize(2);

        // Verify step names
        assertThat(stepExecutions.get(0).getStepName()).isEqualTo("calculateAndApplyInterestStep");
        assertThat(stepExecutions.get(1).getStepName()).isEqualTo("publishEventsStep");

        // Verify both steps completed
        assertThat(stepExecutions.get(0).getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(stepExecutions.get(1).getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    void calculateAndApplyInterestStep_ProcessesActiveAccounts() throws Exception {
        // Arrange: Create accounts with different balances
        Account account1 = createAccount(Account.AccountType.SAVINGS, new BigDecimal("1000.00"));
        Account account2 = createAccount(Account.AccountType.CHECKING, new BigDecimal("2000.00"));
        Account inactiveAccount = createAccount(Account.AccountType.SAVINGS, new BigDecimal("5000.00"));
        inactiveAccount.setStatus(Account.AccountStatus.CLOSED);
        accountRepository.save(inactiveAccount);

        BigDecimal originalBalance1 = account1.getBalance();
        BigDecimal originalBalance2 = account2.getBalance();

        // Act: Launch step
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchStep("calculateAndApplyInterestStep", jobParameters);

        // Assert: Step completed
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // Verify balances increased (interest applied)
        Account updatedAccount1 = accountRepository.findById(account1.getId()).orElseThrow();
        Account updatedAccount2 = accountRepository.findById(account2.getId()).orElseThrow();
        Account updatedInactiveAccount = accountRepository.findById(inactiveAccount.getId()).orElseThrow();

        assertThat(updatedAccount1.getBalance()).isGreaterThan(originalBalance1);
        assertThat(updatedAccount2.getBalance()).isGreaterThan(originalBalance2);
        // Inactive account should not change
        assertThat(updatedInactiveAccount.getBalance()).isEqualTo(new BigDecimal("5000.00"));
    }

    @Test
    void publishEventsStep_CompletesSuccessfully() throws Exception {
        // Act: Launch step
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchStep("publishEventsStep", jobParameters);

        // Assert: Step completed
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    void monthlyInterestJob_HandlesEmptyAccountList() throws Exception {
        // Arrange: No accounts in database

        // Act: Launch job
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Assert: Job still completes successfully
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    void monthlyInterestJob_AppliesCorrectInterestRates() throws Exception {
        // Arrange: Create accounts with known balances
        BigDecimal savingsBalance = new BigDecimal("1000.00");
        BigDecimal checkingBalance = new BigDecimal("1000.00");

        Account savingsAccount = createAccount(Account.AccountType.SAVINGS, savingsBalance);
        Account checkingAccount = createAccount(Account.AccountType.CHECKING, checkingBalance);

        // Act: Launch job
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Assert: Job completed
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // Verify interest rates (SAVINGS: 5%, CHECKING: 1%)
        Account updatedSavings = accountRepository.findById(savingsAccount.getId()).orElseThrow();
        Account updatedChecking = accountRepository.findById(checkingAccount.getId()).orElseThrow();

        // Calculate expected interest
        BigDecimal expectedSavingsInterest = savingsBalance.multiply(new BigDecimal("0.05"));
        BigDecimal expectedCheckingInterest = checkingBalance.multiply(new BigDecimal("0.01"));

        BigDecimal expectedSavingsBalance = savingsBalance.add(expectedSavingsInterest);
        BigDecimal expectedCheckingBalance = checkingBalance.add(expectedCheckingInterest);

        assertThat(updatedSavings.getBalance()).isEqualByComparingTo(expectedSavingsBalance);
        assertThat(updatedChecking.getBalance()).isEqualByComparingTo(expectedCheckingBalance);
    }

    @Test
    void monthlyInterestJob_ProcessesMultipleAccountsInChunks() throws Exception {
        // Arrange: Create 25 accounts (chunk size is 10)
        for (int i = 0; i < 25; i++) {
            createAccount(
                    i % 2 == 0 ? Account.AccountType.SAVINGS : Account.AccountType.CHECKING,
                    new BigDecimal("1000.00")
            );
        }

        // Act: Launch job
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Assert: Job completed successfully
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // Verify all accounts processed
        List<Account> allAccounts = accountRepository.findAll();
        assertThat(allAccounts).hasSize(25);

        // Verify all have interest applied
        for (Account account : allAccounts) {
            assertThat(account.getBalance()).isGreaterThan(new BigDecimal("1000.00"));
        }
    }

    @Test
    void calculateAndApplyInterestStep_ReadsFromRepository() throws Exception {
        // Arrange
        createAccount(Account.AccountType.SAVINGS, new BigDecimal("1000.00"));
        createAccount(Account.AccountType.CHECKING, new BigDecimal("2000.00"));

        // Act: Launch step
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchStep("calculateAndApplyInterestStep", jobParameters);
        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();

        // Assert: Items were read
        assertThat(stepExecution.getReadCount()).isGreaterThan(0);
        assertThat(stepExecution.getWriteCount()).isGreaterThan(0);
    }

    // ========== HELPER METHODS ==========

    private Account createAccount(Account.AccountType type, BigDecimal balance) {
        Account account = new Account();
        account.setCustomerId(testCustomer.getId());
        account.setAccountNumber("4000" + String.format("%08d", (int)(Math.random() * 100000000)));
        account.setAccountType(type);
        account.setBalance(balance);
        account.setStatus(Account.AccountStatus.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());

        // Retry if duplicate account number
        try {
            return accountRepository.save(account);
        } catch (Exception e) {
            // Generate a new unique number
            account.setAccountNumber("4000" + System.nanoTime() % 100000000);
            return accountRepository.save(account);
        }
    }
*/
}
