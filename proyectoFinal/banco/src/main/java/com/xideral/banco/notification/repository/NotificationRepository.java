package com.xideral.banco.notification.repository;

import com.xideral.banco.notification.model.Notification;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    // Derived query methods
    List<Notification> findByCustomerId(Long customerId);

    List<Notification> findByStatus(Notification.NotificationStatus status);

    List<Notification> findByType(Notification.NotificationType type);

    List<Notification> findByChannel(Notification.NotificationChannel channel);

    List<Notification> findByCustomerIdAndStatus(Long customerId, Notification.NotificationStatus status);

    List<Notification> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    // Custom query methods with @Query
    @Query("{ 'customerId': ?0, 'type': ?1 }")
    List<Notification> findByCustomerIdAndType(Long customerId, Notification.NotificationType type);

    @Query("{ 'status': ?0, 'createdAt': { $gte: ?1 } }")
    List<Notification> findPendingNotificationsAfter(Notification.NotificationStatus status, LocalDateTime afterDate);

    @Query("{ 'customerId': ?0, 'createdAt': { $gte: ?1, $lte: ?2 } }")
    List<Notification> findByCustomerIdAndDateRange(Long customerId, LocalDateTime startDate, LocalDateTime endDate);

    @Query(value = "{ 'status': ?0 }", count = true)
    long countByStatus(Notification.NotificationStatus status);

    @Query("{ 'accountNumber': ?0 }")
    List<Notification> findByAccountNumber(String accountNumber);
}