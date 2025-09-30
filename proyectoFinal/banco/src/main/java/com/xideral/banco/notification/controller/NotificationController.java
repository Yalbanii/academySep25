package com.xideral.banco.notification.controller;

import com.xideral.banco.notification.model.Notification;
import com.xideral.banco.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@ConditionalOnBean(MongoTemplate.class)
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestBody Notification notification) {
        Notification created = notificationService.createNotification(notification);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable String id) {
        Notification notification = notificationService.getNotificationById(id);
        return ResponseEntity.ok(notification);
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        List<Notification> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Notification>> getNotificationsByCustomerId(@PathVariable Long customerId) {
        List<Notification> notifications = notificationService.getNotificationsByCustomerId(customerId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/customer/{customerId}/ordered")
    public ResponseEntity<List<Notification>> getNotificationsByCustomerIdOrdered(@PathVariable Long customerId) {
        List<Notification> notifications = notificationService.getNotificationsByCustomerIdOrderByDate(customerId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Notification>> getNotificationsByStatus(@PathVariable Notification.NotificationStatus status) {
        List<Notification> notifications = notificationService.getNotificationsByStatus(status);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Notification>> getNotificationsByType(@PathVariable Notification.NotificationType type) {
        List<Notification> notifications = notificationService.getNotificationsByType(type);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/channel/{channel}")
    public ResponseEntity<List<Notification>> getNotificationsByChannel(@PathVariable Notification.NotificationChannel channel) {
        List<Notification> notifications = notificationService.getNotificationsByChannel(channel);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/customer/{customerId}/status/{status}")
    public ResponseEntity<List<Notification>> getNotificationsByCustomerIdAndStatus(
            @PathVariable Long customerId,
            @PathVariable Notification.NotificationStatus status) {
        List<Notification> notifications = notificationService.getNotificationsByCustomerIdAndStatus(customerId, status);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/customer/{customerId}/type/{type}")
    public ResponseEntity<List<Notification>> getNotificationsByCustomerIdAndType(
            @PathVariable Long customerId,
            @PathVariable Notification.NotificationType type) {
        List<Notification> notifications = notificationService.getNotificationsByCustomerIdAndType(customerId, type);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<Notification>> getNotificationsByAccountNumber(@PathVariable String accountNumber) {
        List<Notification> notifications = notificationService.getNotificationsByAccountNumber(accountNumber);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/pending/after")
    public ResponseEntity<List<Notification>> getPendingNotificationsAfter(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime afterDate) {
        List<Notification> notifications = notificationService.getPendingNotificationsAfter(afterDate);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/customer/{customerId}/daterange")
    public ResponseEntity<List<Notification>> getNotificationsByDateRange(
            @PathVariable Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Notification> notifications = notificationService.getNotificationsByDateRange(customerId, startDate, endDate);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> countByStatus(@PathVariable Notification.NotificationStatus status) {
        long count = notificationService.countByStatus(status);
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable String id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    // Notification sending operations
    @PostMapping("/{id}/send")
    public ResponseEntity<Void> sendNotification(@PathVariable String id) {
        notificationService.sendNotification(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-pending")
    public ResponseEntity<Void> sendPendingNotifications() {
        notificationService.sendPendingNotifications();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/retry-failed")
    public ResponseEntity<Void> retryFailedNotifications() {
        notificationService.retryFailedNotifications();
        return ResponseEntity.ok().build();
    }
}