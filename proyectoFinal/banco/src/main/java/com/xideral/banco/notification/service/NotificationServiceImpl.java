package com.xideral.banco.notification.service;

import com.xideral.banco.notification.model.Notification;
import com.xideral.banco.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public Notification createNotification(Notification notification) {
        log.info("Creating notification for customer: {}", notification.getCustomerId());

        if (notification.getCreatedAt() == null) {
            notification.setCreatedAt(LocalDateTime.now());
        }

        if (notification.getStatus() == null) {
            notification.setStatus(Notification.NotificationStatus.PENDING);
        }

        return notificationRepository.save(notification);
    }

    @Override
    public Notification getNotificationById(String id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + id));
    }

    @Override
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    @Override
    public List<Notification> getNotificationsByCustomerId(Long customerId) {
        return notificationRepository.findByCustomerId(customerId);
    }

    @Override
    public void deleteNotification(String id) {
        if (!notificationRepository.existsById(id)) {
            throw new IllegalArgumentException("Notification not found with id: " + id);
        }
        notificationRepository.deleteById(id);
        log.info("Deleted notification: {}", id);
    }

    @Override
    public List<Notification> getNotificationsByStatus(Notification.NotificationStatus status) {
        return notificationRepository.findByStatus(status);
    }

    @Override
    public List<Notification> getNotificationsByType(Notification.NotificationType type) {
        return notificationRepository.findByType(type);
    }

    @Override
    public List<Notification> getNotificationsByChannel(Notification.NotificationChannel channel) {
        return notificationRepository.findByChannel(channel);
    }

    @Override
    public List<Notification> getNotificationsByCustomerIdAndStatus(Long customerId, Notification.NotificationStatus status) {
        return notificationRepository.findByCustomerIdAndStatus(customerId, status);
    }

    @Override
    public List<Notification> getNotificationsByCustomerIdOrderByDate(Long customerId) {
        return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Override
    public List<Notification> getNotificationsByCustomerIdAndType(Long customerId, Notification.NotificationType type) {
        return notificationRepository.findByCustomerIdAndType(customerId, type);
    }

    @Override
    public List<Notification> getPendingNotificationsAfter(LocalDateTime afterDate) {
        return notificationRepository.findPendingNotificationsAfter(Notification.NotificationStatus.PENDING, afterDate);
    }

    @Override
    public List<Notification> getNotificationsByDateRange(Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        return notificationRepository.findByCustomerIdAndDateRange(customerId, startDate, endDate);
    }

    @Override
    public List<Notification> getNotificationsByAccountNumber(String accountNumber) {
        return notificationRepository.findByAccountNumber(accountNumber);
    }

    @Override
    public long countByStatus(Notification.NotificationStatus status) {
        return notificationRepository.countByStatus(status);
    }

    @Override
    public void sendNotification(String notificationId) {
        Notification notification = getNotificationById(notificationId);

        if (notification.getStatus() == Notification.NotificationStatus.SENT) {
            log.warn("Notification {} already sent", notificationId);
            return;
        }

        try {
            // Simulate sending notification based on channel
            boolean sent = simulateSendNotification(notification);

            if (sent) {
                notification.setStatus(Notification.NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                log.info("Notification sent successfully: {} via {}", notificationId, notification.getChannel());
            } else {
                notification.setStatus(Notification.NotificationStatus.FAILED);
                notification.setErrorMessage("Failed to send notification");
                log.error("Failed to send notification: {}", notificationId);
            }
        } catch (Exception e) {
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            log.error("Error sending notification: {}", notificationId, e);
        }

        notificationRepository.save(notification);
    }

    @Override
    public void sendPendingNotifications() {
        List<Notification> pendingNotifications = getNotificationsByStatus(Notification.NotificationStatus.PENDING);
        log.info("Sending {} pending notifications", pendingNotifications.size());

        for (Notification notification : pendingNotifications) {
            sendNotification(notification.getId());
        }
    }

    @Override
    public void retryFailedNotifications() {
        List<Notification> failedNotifications = getNotificationsByStatus(Notification.NotificationStatus.FAILED);
        log.info("Retrying {} failed notifications", failedNotifications.size());

        for (Notification notification : failedNotifications) {
            notification.setStatus(Notification.NotificationStatus.RETRY);
            notificationRepository.save(notification);
            sendNotification(notification.getId());
        }
    }

    @Override
    public void notifyAccountCreated(Long customerId, String customerEmail, String accountNumber, String accountType) {
        Notification notification = new Notification(
                customerId,
                customerEmail,
                Notification.NotificationType.ACCOUNT_CREATED,
                Notification.NotificationChannel.EMAIL,
                "Cuenta Creada Exitosamente",
                String.format("Su cuenta %s de tipo %s ha sido creada exitosamente.", accountNumber, accountType)
        );
        notification.setAccountNumber(accountNumber);
        notification.setTransactionType(accountType);

        createNotification(notification);
        sendNotification(notification.getId());
    }

    @Override
    public void notifyDeposit(Long customerId, String customerEmail, String accountNumber, String amount) {
        Notification notification = new Notification(
                customerId,
                customerEmail,
                Notification.NotificationType.DEPOSIT,
                Notification.NotificationChannel.EMAIL,
                "Depósito Recibido",
                String.format("Se ha realizado un depósito de $%s en su cuenta %s.", amount, accountNumber)
        );
        notification.setAccountNumber(accountNumber);
        notification.setTransactionType("DEPOSIT");
        notification.setAmount(amount);

        createNotification(notification);
        sendNotification(notification.getId());
    }

    @Override
    public void notifyWithdrawal(Long customerId, String customerEmail, String accountNumber, String amount) {
        Notification notification = new Notification(
                customerId,
                customerEmail,
                Notification.NotificationType.WITHDRAWAL,
                Notification.NotificationChannel.EMAIL,
                "Retiro Realizado",
                String.format("Se ha realizado un retiro de $%s de su cuenta %s.", amount, accountNumber)
        );
        notification.setAccountNumber(accountNumber);
        notification.setTransactionType("WITHDRAWAL");
        notification.setAmount(amount);

        createNotification(notification);
        sendNotification(notification.getId());
    }

    @Override
    public void notifyTransferSent(Long customerId, String customerEmail, String fromAccount, String toAccount, String amount) {
        Notification notification = new Notification(
                customerId,
                customerEmail,
                Notification.NotificationType.TRANSFER_SENT,
                Notification.NotificationChannel.EMAIL,
                "Transferencia Enviada",
                String.format("Se ha transferido $%s de su cuenta %s a la cuenta %s.", amount, fromAccount, toAccount)
        );
        notification.setAccountNumber(fromAccount);
        notification.setTransactionType("TRANSFER_SENT");
        notification.setAmount(amount);

        createNotification(notification);
        sendNotification(notification.getId());
    }

    @Override
    public void notifyTransferReceived(Long customerId, String customerEmail, String fromAccount, String toAccount, String amount) {
        Notification notification = new Notification(
                customerId,
                customerEmail,
                Notification.NotificationType.TRANSFER_RECEIVED,
                Notification.NotificationChannel.EMAIL,
                "Transferencia Recibida",
                String.format("Se ha recibido una transferencia de $%s de la cuenta %s a su cuenta %s.", amount, fromAccount, toAccount)
        );
        notification.setAccountNumber(toAccount);
        notification.setTransactionType("TRANSFER_RECEIVED");
        notification.setAmount(amount);

        createNotification(notification);
        sendNotification(notification.getId());
    }

    @Override
    public void notifyLowBalance(Long customerId, String customerEmail, String accountNumber, String currentBalance) {
        Notification notification = new Notification(
                customerId,
                customerEmail,
                Notification.NotificationType.LOW_BALANCE,
                Notification.NotificationChannel.EMAIL,
                "Saldo Bajo",
                String.format("Su cuenta %s tiene un saldo bajo de $%s.", accountNumber, currentBalance)
        );
        notification.setAccountNumber(accountNumber);
        notification.setTransactionType("LOW_BALANCE");
        notification.setAmount(currentBalance);

        createNotification(notification);
        sendNotification(notification.getId());
    }

    @Override
    public void notifyAccountClosed(Long customerId, String customerEmail, String accountNumber) {
        Notification notification = new Notification(
                customerId,
                customerEmail,
                Notification.NotificationType.ACCOUNT_CLOSED,
                Notification.NotificationChannel.EMAIL,
                "Cuenta Cerrada",
                String.format("Su cuenta %s ha sido cerrada exitosamente.", accountNumber)
        );
        notification.setAccountNumber(accountNumber);
        notification.setTransactionType("ACCOUNT_CLOSED");

        createNotification(notification);
        sendNotification(notification.getId());
    }

    @Override
    public void notifyCustomerRegistered(Long customerId, String customerEmail, String customerName) {
        Notification notification = new Notification(
                customerId,
                customerEmail,
                Notification.NotificationType.CUSTOMER_REGISTERED,
                Notification.NotificationChannel.EMAIL,
                "Bienvenido al Banco Digital",
                String.format("Bienvenido %s, su registro ha sido exitoso. Puede comenzar a utilizar nuestros servicios.", customerName)
        );
        notification.setTransactionType("CUSTOMER_REGISTERED");

        createNotification(notification);
        sendNotification(notification.getId());
    }

    @Override
    public void notifyCustomerUpdated(Long customerId, String customerEmail, String customerName) {
        Notification notification = new Notification(
                customerId,
                customerEmail,
                Notification.NotificationType.CUSTOMER_UPDATED,
                Notification.NotificationChannel.EMAIL,
                "Información Actualizada",
                String.format("Hola %s, su información ha sido actualizada exitosamente.", customerName)
        );
        notification.setTransactionType("CUSTOMER_UPDATED");

        createNotification(notification);
        sendNotification(notification.getId());
    }

    /**
     * Simulates sending a notification based on the channel.
     * In a real application, this would integrate with email services (SendGrid, AWS SES),
     * SMS services (Twilio), or push notification services (Firebase).
     *
     * @param notification The notification to send
     * @return true if simulation succeeds, false otherwise
     */
    private boolean simulateSendNotification(Notification notification) {
        // Simulate sending delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }

        // Simulate sending based on channel with polimorfismo
        return switch (notification.getChannel()) {
            case EMAIL -> simulateEmailSend(notification);
            case SMS -> simulateSmsSend(notification);
            case PUSH -> simulatePushSend(notification);
            case IN_APP -> simulateInAppSend(notification);
        };
    }

    private boolean simulateEmailSend(Notification notification) {
        log.info("📧 EMAIL sent to {}: {}", notification.getCustomerEmail(), notification.getSubject());
        return true;
    }

    private boolean simulateSmsSend(Notification notification) {
        log.info("📱 SMS sent: {}", notification.getMessage());
        return true;
    }

    private boolean simulatePushSend(Notification notification) {
        log.info("🔔 PUSH notification sent: {}", notification.getSubject());
        return true;
    }

    private boolean simulateInAppSend(Notification notification) {
        log.info("💬 IN-APP notification created: {}", notification.getMessage());
        return true;
    }
}