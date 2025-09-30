package com.xideral.banco.notification.repository;

import com.xideral.banco.notification.model.TransactionLog;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@ConditionalOnBean(MongoTemplate.class)
public interface TransactionLogRepository extends MongoRepository<TransactionLog, String> {

    List<TransactionLog> findByAccountNumber(String accountNumber);

    List<TransactionLog> findByTransactionType(String transactionType);

    List<TransactionLog> findByCustomerId(Long customerId);

    List<TransactionLog> findByStatus(String status);

    List<TransactionLog> findByAccountNumberOrderByTimestampDesc(String accountNumber);

    @Query("{ 'timestamp' : { $gte: ?0, $lte: ?1 } }")
    List<TransactionLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    @Query("{ 'accountNumber' : ?0, 'timestamp' : { $gte: ?1, $lte: ?2 } }")
    List<TransactionLog> findByAccountNumberAndTimestampBetween(String accountNumber, LocalDateTime start, LocalDateTime end);

    @Query("{ 'customerId' : ?0, 'timestamp' : { $gte: ?1, $lte: ?2 } }")
    List<TransactionLog> findByCustomerIdAndTimestampBetween(Long customerId, LocalDateTime start, LocalDateTime end);

    @Query("{ 'amount' : { $gte: ?0, $lte: ?1 } }")
    List<TransactionLog> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    long countByTransactionType(String transactionType);

    long countByAccountNumber(String accountNumber);

    long countByStatus(String status);
}
