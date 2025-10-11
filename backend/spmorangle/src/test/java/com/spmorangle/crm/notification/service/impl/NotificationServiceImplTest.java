package com.spmorangle.crm.notification.service.impl;

import com.spmorangle.common.enums.NotificationType;
import com.spmorangle.crm.notification.dto.CreateNotificationDto;
import com.spmorangle.crm.notification.dto.NotificationDto;
import com.spmorangle.crm.notification.dto.NotificationFilterDto;
import com.spmorangle.crm.notification.dto.UnreadCountDto;
import com.spmorangle.crm.notification.enums.Channel;
import com.spmorangle.crm.notification.enums.Priority;
import com.spmorangle.crm.notification.model.Notification;
import com.spmorangle.crm.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationServiceImpl Tests")
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private CreateNotificationDto createNotificationDto;
    private Notification notification;
    private Long userId;
    private Long authorId;

    @BeforeEach
    void setUp() {
        userId = 1L;
        authorId = 2L;

        createNotificationDto = CreateNotificationDto.builder()
                .authorId(authorId)
                .targetId(userId)
                .notificationType(NotificationType.TASK_ASSIGNED)
                .subject("New Task Assigned")
                .message("You have been assigned a new task")
                .channels(Arrays.asList(Channel.EMAIL, Channel.IN_APP))
                .priority(Priority.HIGH)
                .link("/tasks/123")
                .metadata("{\"taskId\": 123}")
                .build();

        notification = Notification.builder()
                .notificationId(1L)
                .authorId(authorId)
                .targetId(userId)
                .notificationType(NotificationType.TASK_ASSIGNED)
                .subject("New Task Assigned")
                .message("You have been assigned a new task")
                .channels(Arrays.asList(Channel.EMAIL, Channel.IN_APP))
                .priority(Priority.HIGH)
                .link("/tasks/123")
                .metadata("{\"taskId\": 123}")
                .readStatus(false)
                .dismissedStatus(false)
                .createdAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("Create Notification Tests")
    class CreateNotificationTests {

        @Test
        @DisplayName("Should create notification successfully")
        void shouldCreateNotificationSuccessfully() {
            // Given
            when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

            // When
            NotificationDto result = notificationService.createNotification(createNotificationDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getNotificationId()).isEqualTo(1L);
            assertThat(result.getSubject()).isEqualTo("New Task Assigned");
            assertThat(result.getMessage()).isEqualTo("You have been assigned a new task");

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());

            Notification savedNotification = captor.getValue();
            assertThat(savedNotification.getAuthorId()).isEqualTo(authorId);
            assertThat(savedNotification.getTargetId()).isEqualTo(userId);
            assertThat(savedNotification.getNotificationType()).isEqualTo(NotificationType.TASK_ASSIGNED);
        }

        @Test
        @DisplayName("Should handle empty metadata")
        void shouldHandleEmptyMetadata() {
            // Given
            createNotificationDto.setMetadata("  ");
            when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

            // When
            NotificationDto result = notificationService.createNotification(createNotificationDto);

            // Then
            assertThat(result).isNotNull();

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            assertThat(captor.getValue().getMetadata()).isNull();
        }

        @Test
        @DisplayName("Should handle null metadata")
        void shouldHandleNullMetadata() {
            // Given
            createNotificationDto.setMetadata(null);
            when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

            // When
            NotificationDto result = notificationService.createNotification(createNotificationDto);

            // Then
            assertThat(result).isNotNull();
            verify(notificationRepository).save(any(Notification.class));
        }
    }

    @Nested
    @DisplayName("Get User Notifications Tests")
    class GetUserNotificationsTests {

        @Test
        @DisplayName("Should get all user notifications")
        void shouldGetAllUserNotifications() {
            // Given
            List<Notification> notifications = Arrays.asList(notification);
            when(notificationRepository.findByTargetIdOrderByCreatedAtDesc(userId)).thenReturn(notifications);

            // When
            List<NotificationDto> result = notificationService.getUserNotifications(userId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getNotificationId()).isEqualTo(1L);
            verify(notificationRepository).findByTargetIdOrderByCreatedAtDesc(userId);
        }

        @Test
        @DisplayName("Should get user notifications with pagination")
        void shouldGetUserNotificationsWithPagination() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Notification> notificationPage = new PageImpl<>(Arrays.asList(notification));
            when(notificationRepository.findByTargetIdOrderByCreatedAtDesc(userId, pageable))
                    .thenReturn(notificationPage);

            // When
            Page<NotificationDto> result = notificationService.getUserNotifications(userId, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getNotificationId()).isEqualTo(1L);
            verify(notificationRepository).findByTargetIdOrderByCreatedAtDesc(userId, pageable);
        }

        @Test
        @DisplayName("Should return empty list when no notifications")
        void shouldReturnEmptyListWhenNoNotifications() {
            // Given
            when(notificationRepository.findByTargetIdOrderByCreatedAtDesc(userId))
                    .thenReturn(Collections.emptyList());

            // When
            List<NotificationDto> result = notificationService.getUserNotifications(userId);

            // Then
            assertThat(result).isEmpty();
            verify(notificationRepository).findByTargetIdOrderByCreatedAtDesc(userId);
        }
    }

    @Nested
    @DisplayName("Get Unread Notifications Tests")
    class GetUnreadNotificationsTests {

        @Test
        @DisplayName("Should get unread notifications")
        void shouldGetUnreadNotifications() {
            // Given
            List<Notification> notifications = Arrays.asList(notification);
            when(notificationRepository.findByTargetIdAndReadStatusFalseOrderByCreatedAtDesc(userId))
                    .thenReturn(notifications);

            // When
            List<NotificationDto> result = notificationService.getUnreadNotifications(userId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getReadStatus()).isFalse();
            verify(notificationRepository).findByTargetIdAndReadStatusFalseOrderByCreatedAtDesc(userId);
        }

        @Test
        @DisplayName("Should get unread notifications with pagination")
        void shouldGetUnreadNotificationsWithPagination() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Notification> notificationPage = new PageImpl<>(Arrays.asList(notification));
            when(notificationRepository.findByTargetIdAndReadStatusFalseOrderByCreatedAtDesc(userId, pageable))
                    .thenReturn(notificationPage);

            // When
            Page<NotificationDto> result = notificationService.getUnreadNotifications(userId, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getReadStatus()).isFalse();
            verify(notificationRepository).findByTargetIdAndReadStatusFalseOrderByCreatedAtDesc(userId, pageable);
        }
    }

    @Nested
    @DisplayName("Get Notifications With Filters Tests")
    class GetNotificationsWithFiltersTests {

        @Test
        @DisplayName("Should get notifications with filters")
        void shouldGetNotificationsWithFilters() {
            // Given
            NotificationFilterDto filters = NotificationFilterDto.builder()
                    .unreadOnly(true)
                    .activeOnly(true)
                    .notificationType(NotificationType.TASK_ASSIGNED)
                    .priority(Priority.HIGH)
                    .build();

            Pageable pageable = PageRequest.of(0, 10);
            Page<Notification> notificationPage = new PageImpl<>(Arrays.asList(notification));

            when(notificationRepository.findWithFilters(
                    eq(userId), eq(true), eq(true), eq(NotificationType.TASK_ASSIGNED), eq(Priority.HIGH), eq(pageable)))
                    .thenReturn(notificationPage);

            // When
            Page<NotificationDto> result = notificationService.getNotificationsWithFilters(userId, filters, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            verify(notificationRepository).findWithFilters(
                    userId, true, true, NotificationType.TASK_ASSIGNED, Priority.HIGH, pageable);
        }
    }

    @Nested
    @DisplayName("Get Notification By ID Tests")
    class GetNotificationByIdTests {

        @Test
        @DisplayName("Should get notification by ID successfully")
        void shouldGetNotificationByIdSuccessfully() {
            // Given
            when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

            // When
            NotificationDto result = notificationService.getNotificationById(1L, userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getNotificationId()).isEqualTo(1L);
            verify(notificationRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when notification not found")
        void shouldThrowExceptionWhenNotificationNotFound() {
            // Given
            when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> notificationService.getNotificationById(1L, userId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Notification not found");
        }

        @Test
        @DisplayName("Should throw exception when user not authorized")
        void shouldThrowExceptionWhenUserNotAuthorized() {
            // Given
            Long unauthorizedUserId = 999L;
            when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

            // When/Then
            assertThatThrownBy(() -> notificationService.getNotificationById(1L, unauthorizedUserId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not authorized");
        }
    }

    @Nested
    @DisplayName("Mark As Read Tests")
    class MarkAsReadTests {

        @Test
        @DisplayName("Should mark single notification as read")
        void shouldMarkSingleNotificationAsRead() {
            // Given
            when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
            when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

            // When
            notificationService.markAsRead(1L, userId);

            // Then
            verify(notificationRepository).findById(1L);
            verify(notificationRepository).save(any(Notification.class));
        }

        @Test
        @DisplayName("Should not save when notification already read")
        void shouldNotSaveWhenNotificationAlreadyRead() {
            // Given
            notification.markAsRead();
            when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

            // When
            notificationService.markAsRead(1L, userId);

            // Then
            verify(notificationRepository).findById(1L);
            verify(notificationRepository, never()).save(any(Notification.class));
        }

        @Test
        @DisplayName("Should throw exception when marking as read unauthorized")
        void shouldThrowExceptionWhenMarkingAsReadUnauthorized() {
            // Given
            Long unauthorizedUserId = 999L;
            when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

            // When/Then
            assertThatThrownBy(() -> notificationService.markAsRead(1L, unauthorizedUserId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not authorized");
        }

        @Test
        @DisplayName("Should mark multiple notifications as read")
        void shouldMarkMultipleNotificationsAsRead() {
            // Given
            List<Long> notificationIds = Arrays.asList(1L, 2L, 3L);
            when(notificationRepository.markAsReadByIds(eq(notificationIds), eq(userId), any(Instant.class), any(Instant.class)))
                    .thenReturn(3);

            // When
            notificationService.markAsRead(notificationIds, userId);

            // Then
            verify(notificationRepository).markAsReadByIds(eq(notificationIds), eq(userId), any(Instant.class), any(Instant.class));
        }

        @Test
        @DisplayName("Should mark all notifications as read")
        void shouldMarkAllNotificationsAsRead() {
            // Given
            when(notificationRepository.markAllAsReadByTargetId(eq(userId), any(Instant.class), any(Instant.class)))
                    .thenReturn(5);

            // When
            notificationService.markAllAsRead(userId);

            // Then
            verify(notificationRepository).markAllAsReadByTargetId(eq(userId), any(Instant.class), any(Instant.class));
        }
    }

    @Nested
    @DisplayName("Dismiss Notification Tests")
    class DismissNotificationTests {

        @Test
        @DisplayName("Should dismiss notification successfully")
        void shouldDismissNotificationSuccessfully() {
            // Given
            when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
            when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

            // When
            notificationService.dismissNotification(1L, userId);

            // Then
            verify(notificationRepository).findById(1L);
            verify(notificationRepository).save(any(Notification.class));
        }

        @Test
        @DisplayName("Should not save when notification already dismissed")
        void shouldNotSaveWhenNotificationAlreadyDismissed() {
            // Given
            notification.markAsDismissed();
            when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

            // When
            notificationService.dismissNotification(1L, userId);

            // Then
            verify(notificationRepository).findById(1L);
            verify(notificationRepository, never()).save(any(Notification.class));
        }

        @Test
        @DisplayName("Should throw exception when dismissing unauthorized")
        void shouldThrowExceptionWhenDismissingUnauthorized() {
            // Given
            Long unauthorizedUserId = 999L;
            when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

            // When/Then
            assertThatThrownBy(() -> notificationService.dismissNotification(1L, unauthorizedUserId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not authorized");
        }
    }

    @Nested
    @DisplayName("Unread Count Tests")
    class UnreadCountTests {

        @Test
        @DisplayName("Should get unread count")
        void shouldGetUnreadCount() {
            // Given
            when(notificationRepository.countUnreadByTargetId(userId)).thenReturn(5L);

            // When
            UnreadCountDto result = notificationService.getUnreadCount(userId);

            // Then
            assertThat(result.getCount()).isEqualTo(5);
            verify(notificationRepository).countUnreadByTargetId(userId);
        }

        @Test
        @DisplayName("Should return zero when no unread notifications")
        void shouldReturnZeroWhenNoUnreadNotifications() {
            // Given
            when(notificationRepository.countUnreadByTargetId(userId)).thenReturn(0L);

            // When
            UnreadCountDto result = notificationService.getUnreadCount(userId);

            // Then
            assertThat(result.getCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Delete Notification Tests")
    class DeleteNotificationTests {

        @Test
        @DisplayName("Should delete notification")
        void shouldDeleteNotification() {
            // Given
            doNothing().when(notificationRepository).deleteById(1L);

            // When
            notificationService.deleteNotification(1L);

            // Then
            verify(notificationRepository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("Cleanup Old Notifications Tests")
    class CleanupOldNotificationsTests {

        @Test
        @DisplayName("Should cleanup old notifications")
        void shouldCleanupOldNotifications() {
            // Given
            int daysToKeep = 30;
            when(notificationRepository.deleteOldReadNotifications(any(Instant.class))).thenReturn(10);

            // When
            int result = notificationService.cleanupOldNotifications(daysToKeep);

            // Then
            assertThat(result).isEqualTo(10);

            ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
            verify(notificationRepository).deleteOldReadNotifications(captor.capture());

            Instant cutoffDate = captor.getValue();
            Instant expectedCutoff = Instant.now().minus(daysToKeep, ChronoUnit.DAYS);
            assertThat(cutoffDate).isCloseTo(expectedCutoff, within(1, ChronoUnit.SECONDS));
        }
    }

    @Nested
    @DisplayName("Recent Similar Notification Tests")
    class RecentSimilarNotificationTests {

        @Test
        @DisplayName("Should find recent similar notification")
        void shouldFindRecentSimilarNotification() {
            // Given
            int minutesBack = 5;
            when(notificationRepository.findRecentByAuthorAndType(
                    eq(authorId), eq(userId), eq(NotificationType.TASK_ASSIGNED), any(Instant.class)))
                    .thenReturn(Arrays.asList(notification));

            // When
            boolean result = notificationService.hasRecentSimilarNotification(
                    authorId, userId, NotificationType.TASK_ASSIGNED, minutesBack);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should not find recent similar notification")
        void shouldNotFindRecentSimilarNotification() {
            // Given
            int minutesBack = 5;
            when(notificationRepository.findRecentByAuthorAndType(
                    eq(authorId), eq(userId), eq(NotificationType.TASK_ASSIGNED), any(Instant.class)))
                    .thenReturn(Collections.emptyList());

            // When
            boolean result = notificationService.hasRecentSimilarNotification(
                    authorId, userId, NotificationType.TASK_ASSIGNED, minutesBack);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Bulk Create Notifications Tests")
    class BulkCreateNotificationsTests {

        @Test
        @DisplayName("Should create bulk notifications successfully")
        void shouldCreateBulkNotificationsSuccessfully() {
            // Given
            CreateNotificationDto dto2 = CreateNotificationDto.builder()
                    .authorId(authorId)
                    .targetId(3L)
                    .notificationType(NotificationType.TASK_COMPLETED)
                    .subject("Task Completed")
                    .message("Task has been completed")
                    .channels(Arrays.asList(Channel.EMAIL))
                    .priority(Priority.MEDIUM)
                    .build();

            List<CreateNotificationDto> dtos = Arrays.asList(createNotificationDto, dto2);

            Notification notification2 = Notification.builder()
                    .notificationId(2L)
                    .authorId(authorId)
                    .targetId(3L)
                    .notificationType(NotificationType.TASK_COMPLETED)
                    .subject("Task Completed")
                    .message("Task has been completed")
                    .channels(Arrays.asList(Channel.EMAIL))
                    .priority(Priority.MEDIUM)
                    .readStatus(false)
                    .dismissedStatus(false)
                    .createdAt(Instant.now())
                    .build();

            when(notificationRepository.saveAll(anyList()))
                    .thenReturn(Arrays.asList(notification, notification2));

            // When
            List<NotificationDto> result = notificationService.createBulkNotifications(dtos);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getNotificationId()).isEqualTo(1L);
            assertThat(result.get(1).getNotificationId()).isEqualTo(2L);
            verify(notificationRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("Should handle empty metadata in bulk create")
        void shouldHandleEmptyMetadataInBulkCreate() {
            // Given
            createNotificationDto.setMetadata("  ");
            List<CreateNotificationDto> dtos = Arrays.asList(createNotificationDto);

            when(notificationRepository.saveAll(anyList())).thenReturn(Arrays.asList(notification));

            // When
            List<NotificationDto> result = notificationService.createBulkNotifications(dtos);

            // Then
            assertThat(result).hasSize(1);

            ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
            verify(notificationRepository).saveAll(captor.capture());
            assertThat(captor.getValue().get(0).getMetadata()).isNull();
        }
    }

    @Nested
    @DisplayName("Get Notifications By Type Tests")
    class GetNotificationsByTypeTests {

        @Test
        @DisplayName("Should get notifications by type")
        void shouldGetNotificationsByType() {
            // Given
            List<Notification> notifications = Arrays.asList(notification);
            when(notificationRepository.findByTargetIdAndNotificationTypeOrderByCreatedAtDesc(
                    userId, NotificationType.TASK_ASSIGNED))
                    .thenReturn(notifications);

            // When
            List<NotificationDto> result = notificationService.getNotificationsByType(
                    userId, NotificationType.TASK_ASSIGNED);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getNotificationType()).isEqualTo(NotificationType.TASK_ASSIGNED);
        }
    }
}
