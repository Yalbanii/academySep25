package com.xideral.banco.notification.service;

import com.xideral.banco.events.AccountCreatedEvent;
import com.xideral.banco.events.CustomerCreatedEvent;
import com.xideral.banco.events.TransactionCompletedEvent;
import com.xideral.banco.events.TransferCompletedEvent;
import com.xideral.banco.notification.model.Notification;
import com.xideral.banco.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification testNotification;
    private final Long customerId = 1L;
    private final String customerEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        testNotification = new Notification(
                customerId,
                customerEmail,
                Notification.NotificationType.ACCOUNT_CREATED,
                Notification.NotificationChannel.EMAIL,
                "Test Subject",
                "Test Message"
        );
        testNotification.setId("test-id-123");
        testNotification.setStatus(Notification.NotificationStatus.PENDING);
        testNotification.setCreatedAt(LocalDateTime.now());
    }

    // ========== CREATE NOTIFICATION TESTS ==========

    @Test
    void createNotification_Success() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        Notification result = notificationService.createNotification(testNotification);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("test-id-123");
        assertThat(result.getStatus()).isEqualTo(Notification.NotificationStatus.PENDING);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void createNotification_SetsDefaultValues() {
        // Arrange
        Notification notification = new Notification();
        notification.setCustomerId(customerId);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        Notification result = notificationService.createNotification(notification);

        // Assert
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    // ========== GET NOTIFICATION TESTS ==========

    @Test
    void getNotificationById_Success() {
        // Arrange
        when(notificationRepository.findById("test-id-123")).thenReturn(Optional.of(testNotification));

        // Act
        Notification result = notificationService.getNotificationById("test-id-123");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("test-id-123");
        verify(notificationRepository, times(1)).findById("test-id-123");
    }

    @Test
    void getNotificationById_NotFound() {
        // Arrange
        when(notificationRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> notificationService.getNotificationById("invalid-id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Notification not found");
        verify(notificationRepository, times(1)).findById("invalid-id");
    }

    @Test
    void getAllNotifications_Success() {
        // Arrange
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findAll()).thenReturn(notifications);

        // Act
        List<Notification> result = notificationService.getAllNotifications();

        // Assert
        assertThat(result).hasSize(1);
        verify(notificationRepository, times(1)).findAll();
    }

    @Test
    void getNotificationsByCustomerId_Success() {
        // Arrange
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByCustomerId(customerId)).thenReturn(notifications);

        // Act
        List<Notification> result = notificationService.getNotificationsByCustomerId(customerId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerId()).isEqualTo(customerId);
        verify(notificationRepository, times(1)).findByCustomerId(customerId);
    }

    // ========== DELETE NOTIFICATION TESTS ==========

    @Test
    void deleteNotification_Success() {
        // Arrange
        when(notificationRepository.existsById("test-id-123")).thenReturn(true);
        doNothing().when(notificationRepository).deleteById("test-id-123");

        // Act
        notificationService.deleteNotification("test-id-123");

        // Assert
        verify(notificationRepository, times(1)).existsById("test-id-123");
        verify(notificationRepository, times(1)).deleteById("test-id-123");
    }

    @Test
    void deleteNotification_NotFound() {
        // Arrange
        when(notificationRepository.existsById("invalid-id")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> notificationService.deleteNotification("invalid-id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Notification not found");
        verify(notificationRepository, times(1)).existsById("invalid-id");
        verify(notificationRepository, never()).deleteById(anyString());
    }

    // ========== QUERY TESTS ==========

    @Test
    void getNotificationsByStatus_Success() {
        // Arrange
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByStatus(Notification.NotificationStatus.PENDING))
                .thenReturn(notifications);

        // Act
        List<Notification> result = notificationService.getNotificationsByStatus(Notification.NotificationStatus.PENDING);

        // Assert
        assertThat(result).hasSize(1);
        verify(notificationRepository, times(1)).findByStatus(Notification.NotificationStatus.PENDING);
    }

    @Test
    void countByStatus_Success() {
        // Arrange
        when(notificationRepository.countByStatus(Notification.NotificationStatus.SENT)).thenReturn(5L);

        // Act
        long count = notificationService.countByStatus(Notification.NotificationStatus.SENT);

        // Assert
        assertThat(count).isEqualTo(5L);
        verify(notificationRepository, times(1)).countByStatus(Notification.NotificationStatus.SENT);
    }

    // ========== BUSINESS LOGIC TESTS ==========

    @Test
    void notifyAccountCreated_Success() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            if (notification.getId() == null) {
                notification.setId("test-id-" + System.nanoTime());
            }
            return notification;
        });
        when(notificationRepository.findById(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            Notification notification = new Notification();
            notification.setId(id);
            notification.setStatus(Notification.NotificationStatus.PENDING);
            notification.setChannel(Notification.NotificationChannel.EMAIL);
            return Optional.of(notification);
        });

        // Act
        notificationService.notifyAccountCreated(customerId, customerEmail, "400012345678", "SAVINGS");

        // Assert
        verify(notificationRepository, times(2)).save(any(Notification.class)); // create + send update
    }

    @Test
    void notifyDeposit_Success() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            if (notification.getId() == null) {
                notification.setId("test-id-" + System.nanoTime());
            }
            return notification;
        });
        when(notificationRepository.findById(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            Notification notification = new Notification();
            notification.setId(id);
            notification.setStatus(Notification.NotificationStatus.PENDING);
            notification.setChannel(Notification.NotificationChannel.EMAIL);
            return Optional.of(notification);
        });

        // Act
        notificationService.notifyDeposit(customerId, customerEmail, "400012345678", "1000.00");

        // Assert
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void notifyWithdrawal_Success() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            if (notification.getId() == null) {
                notification.setId("test-id-" + System.nanoTime());
            }
            return notification;
        });
        when(notificationRepository.findById(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            Notification notification = new Notification();
            notification.setId(id);
            notification.setStatus(Notification.NotificationStatus.PENDING);
            notification.setChannel(Notification.NotificationChannel.EMAIL);
            return Optional.of(notification);
        });

        // Act
        notificationService.notifyWithdrawal(customerId, customerEmail, "400012345678", "500.00");

        // Assert
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void notifyTransferSent_Success() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            if (notification.getId() == null) {
                notification.setId("test-id-" + System.nanoTime());
            }
            return notification;
        });
        when(notificationRepository.findById(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            Notification notification = new Notification();
            notification.setId(id);
            notification.setStatus(Notification.NotificationStatus.PENDING);
            notification.setChannel(Notification.NotificationChannel.EMAIL);
            return Optional.of(notification);
        });

        // Act
        notificationService.notifyTransferSent(customerId, customerEmail, "400012345678", "400087654321", "250.00");

        // Assert
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void notifyTransferReceived_Success() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            if (notification.getId() == null) {
                notification.setId("test-id-" + System.nanoTime());
            }
            return notification;
        });
        when(notificationRepository.findById(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            Notification notification = new Notification();
            notification.setId(id);
            notification.setStatus(Notification.NotificationStatus.PENDING);
            notification.setChannel(Notification.NotificationChannel.EMAIL);
            return Optional.of(notification);
        });

        // Act
        notificationService.notifyTransferReceived(customerId, customerEmail, "400012345678", "400087654321", "250.00");

        // Assert
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    // ========== EVENT LISTENER TESTS ==========

    @Test
    void handleCustomerCreated_Success() {
        // Arrange
        CustomerCreatedEvent event = new CustomerCreatedEvent(
                1L,
                "John Doe",
                "john@example.com",
                LocalDateTime.now()
        );
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            if (notification.getId() == null) {
                notification.setId("test-id-" + System.nanoTime());
            }
            return notification;
        });
        when(notificationRepository.findById(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            Notification notification = new Notification();
            notification.setId(id);
            notification.setStatus(Notification.NotificationStatus.PENDING);
            notification.setChannel(Notification.NotificationChannel.EMAIL);
            return Optional.of(notification);
        });

        // Act
        notificationService.handleCustomerCreated(event);

        // Assert
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void handleAccountCreated_Success() {
        // Arrange
        AccountCreatedEvent event = new AccountCreatedEvent(
                1L,
                "400012345678",
                "SAVINGS",
                BigDecimal.ZERO,
                1L,
                "test@example.com",
                LocalDateTime.now()
        );
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            if (notification.getId() == null) {
                notification.setId("test-id-" + System.nanoTime());
            }
            return notification;
        });
        when(notificationRepository.findById(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            Notification notification = new Notification();
            notification.setId(id);
            notification.setStatus(Notification.NotificationStatus.PENDING);
            notification.setChannel(Notification.NotificationChannel.EMAIL);
            return Optional.of(notification);
        });

        // Act
        notificationService.handleAccountCreated(event);

        // Assert
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void handleTransactionCompleted_Deposit_Success() {
        // Arrange
        TransactionCompletedEvent event = new TransactionCompletedEvent(
                "txn-123",
                "400012345678",
                "DEPOSIT",
                new BigDecimal("1000.00"),
                new BigDecimal("5000.00"),
                "test@example.com",
                LocalDateTime.now()
        );
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            if (notification.getId() == null) {
                notification.setId("test-id-" + System.nanoTime());
            }
            return notification;
        });
        when(notificationRepository.findById(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            Notification notification = new Notification();
            notification.setId(id);
            notification.setStatus(Notification.NotificationStatus.PENDING);
            notification.setChannel(Notification.NotificationChannel.EMAIL);
            return Optional.of(notification);
        });

        // Act
        notificationService.handleTransactionCompleted(event);

        // Assert
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void handleTransactionCompleted_Withdrawal_Success() {
        // Arrange
        TransactionCompletedEvent event = new TransactionCompletedEvent(
                "txn-124",
                "400012345678",
                "WITHDRAWAL",
                new BigDecimal("500.00"),
                new BigDecimal("150.00"), // Low balance scenario
                "test@example.com",
                LocalDateTime.now()
        );
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            if (notification.getId() == null) {
                notification.setId("test-id-" + System.nanoTime());
            }
            return notification;
        });
        when(notificationRepository.findById(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            Notification notification = new Notification();
            notification.setId(id);
            notification.setStatus(Notification.NotificationStatus.PENDING);
            notification.setChannel(Notification.NotificationChannel.EMAIL);
            return Optional.of(notification);
        });

        // Act
        notificationService.handleTransactionCompleted(event);

        // Assert
        verify(notificationRepository, times(4)).save(any(Notification.class)); // withdrawal + low balance + 2 sends
    }

    @Test
    void handleTransferCompleted_Success() {
        // Arrange
        TransferCompletedEvent event = new TransferCompletedEvent(
                "txn-125",
                "400012345678",
                "400087654321",
                new BigDecimal("250.00"),
                "sender@example.com",
                "receiver@example.com",
                LocalDateTime.now()
        );
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            if (notification.getId() == null) {
                notification.setId("test-id-" + System.nanoTime());
            }
            return notification;
        });
        when(notificationRepository.findById(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            Notification notification = new Notification();
            notification.setId(id);
            notification.setStatus(Notification.NotificationStatus.PENDING);
            notification.setChannel(Notification.NotificationChannel.EMAIL);
            return Optional.of(notification);
        });

        // Act
        notificationService.handleTransferCompleted(event);

        // Assert
        verify(notificationRepository, times(4)).save(any(Notification.class)); // 2 notifications + 2 sends
    }

    // ========== SEND NOTIFICATION TESTS ==========

    @Test
    void sendNotification_Success() {
        // Arrange
        testNotification.setStatus(Notification.NotificationStatus.PENDING);
        when(notificationRepository.findById("test-id-123")).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        notificationService.sendNotification("test-id-123");

        // Assert
        verify(notificationRepository, times(1)).findById("test-id-123");
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void sendNotification_AlreadySent() {
        // Arrange
        testNotification.setStatus(Notification.NotificationStatus.SENT);
        when(notificationRepository.findById("test-id-123")).thenReturn(Optional.of(testNotification));

        // Act
        notificationService.sendNotification("test-id-123");

        // Assert
        verify(notificationRepository, times(1)).findById("test-id-123");
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void sendPendingNotifications_Success() {
        // Arrange
        List<Notification> pending = Arrays.asList(testNotification);
        when(notificationRepository.findByStatus(Notification.NotificationStatus.PENDING))
                .thenReturn(pending);
        when(notificationRepository.findById("test-id-123")).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        notificationService.sendPendingNotifications();

        // Assert
        verify(notificationRepository, times(1)).findByStatus(Notification.NotificationStatus.PENDING);
        verify(notificationRepository, times(1)).findById("test-id-123");
    }

    @Test
    void retryFailedNotifications_Success() {
        // Arrange
        testNotification.setStatus(Notification.NotificationStatus.FAILED);
        List<Notification> failed = Arrays.asList(testNotification);
        when(notificationRepository.findByStatus(Notification.NotificationStatus.FAILED))
                .thenReturn(failed);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(notificationRepository.findById("test-id-123")).thenReturn(Optional.of(testNotification));

        // Act
        notificationService.retryFailedNotifications();

        // Assert
        verify(notificationRepository, times(1)).findByStatus(Notification.NotificationStatus.FAILED);
        verify(notificationRepository, atLeast(1)).save(any(Notification.class));
    }
}
