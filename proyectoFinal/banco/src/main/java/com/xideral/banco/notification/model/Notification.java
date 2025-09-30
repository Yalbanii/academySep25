package com.xideral.banco.notification.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    private Long customerId;
    private String customerEmail;
    private NotificationType type;
    private NotificationChannel channel;
    private String subject;
    private String message;
    private NotificationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private String errorMessage;

    // Metadata fields
    private String accountNumber;
    private String transactionType;
    private String amount;

    public enum NotificationType {
        ACCOUNT_CREATED,
        ACCOUNT_CLOSED,
        DEPOSIT,
        WITHDRAWAL,
        TRANSFER_SENT,
        TRANSFER_RECEIVED,
        LOW_BALANCE,
        CUSTOMER_REGISTERED,
        CUSTOMER_UPDATED
    }

    public enum NotificationChannel {
        EMAIL,
        SMS,
        PUSH,
        IN_APP
    }

    public enum NotificationStatus {
        PENDING,
        SENT,
        FAILED,
        RETRY
    }

    public Notification(Long customerId, String customerEmail, NotificationType type,
                       NotificationChannel channel, String subject, String message) {
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.type = type;
        this.channel = channel;
        this.subject = subject;
        this.message = message;
        this.status = NotificationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }
}