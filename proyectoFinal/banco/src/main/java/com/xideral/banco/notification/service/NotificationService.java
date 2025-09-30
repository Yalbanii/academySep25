package com.xideral.banco.notification.service;

import com.xideral.banco.notification.model.Notification;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationService {

    // CRUD operations
    Notification createNotification(Notification notification);

    Notification getNotificationById(String id);

    List<Notification> getAllNotifications();

    List<Notification> getNotificationsByCustomerId(Long customerId);

    void deleteNotification(String id);

    // Query operations
    List<Notification> getNotificationsByStatus(Notification.NotificationStatus status);

    List<Notification> getNotificationsByType(Notification.NotificationType type);

    List<Notification> getNotificationsByChannel(Notification.NotificationChannel channel);

    List<Notification> getNotificationsByCustomerIdAndStatus(Long customerId, Notification.NotificationStatus status);

    List<Notification> getNotificationsByCustomerIdOrderByDate(Long customerId);

    List<Notification> getNotificationsByCustomerIdAndType(Long customerId, Notification.NotificationType type);

    List<Notification> getPendingNotificationsAfter(LocalDateTime afterDate);

    List<Notification> getNotificationsByDateRange(Long customerId, LocalDateTime startDate, LocalDateTime endDate);

    List<Notification> getNotificationsByAccountNumber(String accountNumber);

    long countByStatus(Notification.NotificationStatus status);

    // Notification sending operations
    void sendNotification(String notificationId);

    void sendPendingNotifications();

    void retryFailedNotifications();

    // Business operations - Account events
    void notifyAccountCreated(Long customerId, String customerEmail, String accountNumber, String accountType);

    void notifyDeposit(Long customerId, String customerEmail, String accountNumber, String amount);

    void notifyWithdrawal(Long customerId, String customerEmail, String accountNumber, String amount);

    void notifyTransferSent(Long customerId, String customerEmail, String fromAccount, String toAccount, String amount);

    void notifyTransferReceived(Long customerId, String customerEmail, String fromAccount, String toAccount, String amount);

    void notifyLowBalance(Long customerId, String customerEmail, String accountNumber, String currentBalance);

    void notifyAccountClosed(Long customerId, String customerEmail, String accountNumber);

    // Business operations - Customer events
    void notifyCustomerRegistered(Long customerId, String customerEmail, String customerName);

    void notifyCustomerUpdated(Long customerId, String customerEmail, String customerName);
}