package com.spmorangle.crm.notification.messaging.consumer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.spmorangle.common.config.FrontendConfig;
import com.spmorangle.common.enums.NotificationType;
import com.spmorangle.crm.notification.dto.CreateNotificationDto;
import com.spmorangle.crm.notification.dto.NotificationDto;
import com.spmorangle.crm.notification.enums.Channel;
import com.spmorangle.crm.notification.enums.Priority;
import com.spmorangle.crm.notification.messaging.dto.CommentNotificationMessageDto;
import com.spmorangle.crm.notification.service.EmailService;
import com.spmorangle.crm.notification.service.NotificationService;
import com.spmorangle.crm.taskmanagement.repository.TaskAssigneeRepository;
import com.spmorangle.crm.usermanagement.dto.UserResponseDto;
import com.spmorangle.crm.usermanagement.service.UserManagementService;

@ExtendWith(MockitoExtension.class)
class CommentNotificationConsumerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserManagementService userManagementService;

    @Mock
    private EmailService emailService;

    @Mock
    private TaskAssigneeRepository taskAssigneeRepository;

    @Mock(lenient = true)
    private FrontendConfig frontendConfig;

    @InjectMocks
    private CommentNotificationConsumer commentNotificationConsumer;

    private CommentNotificationMessageDto commentCreatedMessage;
    private CommentNotificationMessageDto commentWithMentionsMessage;
    private CommentNotificationMessageDto mentionEventMessage;

    @BeforeEach
    void setUp() {
        // Mock frontend config
        when(frontendConfig.getBaseUrl()).thenReturn("http://localhost:3000");

        // Setup basic comment created message without mentions
        commentCreatedMessage = CommentNotificationMessageDto.builder()
                .messageId("msg-123")
                .eventType("COMMENT_CREATED")
                .commentId(1L)
                .taskId(10L)
                .authorId(100L)
                .content("This is a test comment")
                .taskTitle("Test Task")
                .timestamp(OffsetDateTime.now())
                .build();

        // Setup comment with mentions
        commentWithMentionsMessage = CommentNotificationMessageDto.builder()
                .messageId("msg-456")
                .eventType("COMMENT_CREATED")
                .commentId(2L)
                .taskId(10L)
                .authorId(100L)
                .content("Hey @user1 and @user2, check this out!")
                .mentionedUserIds(List.of(200L, 300L))
                .taskTitle("Test Task")
                .timestamp(OffsetDateTime.now())
                .build();

        // Setup mention event message
        mentionEventMessage = CommentNotificationMessageDto.builder()
                .messageId("msg-789")
                .eventType("MENTION")
                .commentId(3L)
                .taskId(10L)
                .authorId(100L)
                .content("@user1 please review")
                .mentionedUserIds(List.of(200L))
                .taskTitle("Review Task")
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @Test
    void testHandleCommentCreated_NoMentions() {
        // Arrange
        when(taskAssigneeRepository.findAssigneeIdsByTaskId(10L))
                .thenReturn(List.of(200L, 300L));

        NotificationDto notification1 = createMockNotificationDto(1L, 200L, "New comment");
        NotificationDto notification2 = createMockNotificationDto(2L, 300L, "New comment");

        when(notificationService.createBulkNotifications(anyList()))
                .thenReturn(List.of(notification1, notification2));

        when(userManagementService.getUserById(200L))
                .thenReturn(new UserResponseDto(200L, "User1", "user1@test.com", null, null));
        when(userManagementService.getUserById(300L))
                .thenReturn(new UserResponseDto(300L, "User2", "user2@test.com", null, null));

        // Act
        commentNotificationConsumer.handleCommentNotification(commentCreatedMessage);

        // Assert
        ArgumentCaptor<List<CreateNotificationDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationService).createBulkNotifications(captor.capture());

        List<CreateNotificationDto> capturedNotifications = captor.getValue();
        assert capturedNotifications.size() == 2;
        assert capturedNotifications.get(0).getTargetId().equals(200L);
        assert capturedNotifications.get(1).getTargetId().equals(300L);
        assert capturedNotifications.get(0).getNotificationType() == NotificationType.COMMENT_REPLY;

        verify(emailService, times(2)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testHandleCommentCreated_WithMentions() {
        // Arrange
        when(taskAssigneeRepository.findAssigneeIdsByTaskId(10L))
                .thenReturn(List.of(200L, 300L, 400L)); // 400L is assignee but not mentioned

        NotificationDto mention1 = createMockNotificationDto(1L, 200L, "You were mentioned");
        NotificationDto mention2 = createMockNotificationDto(2L, 300L, "You were mentioned");
        NotificationDto assignee = createMockNotificationDto(3L, 400L, "New comment");

        when(notificationService.createBulkNotifications(anyList()))
                .thenReturn(List.of(mention1, mention2, assignee));

        when(userManagementService.getUserById(200L))
                .thenReturn(new UserResponseDto(200L, "User1", "user1@test.com", null, null));
        when(userManagementService.getUserById(300L))
                .thenReturn(new UserResponseDto(300L, "User2", "user2@test.com", null, null));
        when(userManagementService.getUserById(400L))
                .thenReturn(new UserResponseDto(400L, "User3", "user3@test.com", null, null));

        // Act
        commentNotificationConsumer.handleCommentNotification(commentWithMentionsMessage);

        // Assert
        ArgumentCaptor<List<CreateNotificationDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationService).createBulkNotifications(captor.capture());

        List<CreateNotificationDto> capturedNotifications = captor.getValue();
        assert capturedNotifications.size() == 5; // 2 mentions + 3 assignees (no deduplication after our changes)

        // Verify mentions have HIGH priority
        long mentionNotifications = capturedNotifications.stream()
                .filter(n -> n.getNotificationType() == NotificationType.MENTION)
                .count();
        assert mentionNotifications == 2;

        verify(emailService, times(3)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testHandleMentionEvent() {
        // Arrange
        NotificationDto notification = createMockNotificationDto(1L, 200L, "You were mentioned");

        when(notificationService.createBulkNotifications(anyList()))
                .thenReturn(List.of(notification));

        when(userManagementService.getUserById(200L))
                .thenReturn(new UserResponseDto(200L, "User1", "user1@test.com", null, null));

        // Act
        commentNotificationConsumer.handleCommentNotification(mentionEventMessage);

        // Assert
        ArgumentCaptor<List<CreateNotificationDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationService).createBulkNotifications(captor.capture());

        List<CreateNotificationDto> capturedNotifications = captor.getValue();
        assert capturedNotifications.size() == 1;
        assert capturedNotifications.get(0).getTargetId().equals(200L);
        assert capturedNotifications.get(0).getNotificationType() == NotificationType.MENTION;

        verify(emailService).sendEmail(eq("user1@test.com"), anyString(), anyString());
    }

    @Test
    void testHandleCommentCreated_NoAssignees() {
        // Arrange
        when(taskAssigneeRepository.findAssigneeIdsByTaskId(10L))
                .thenReturn(new ArrayList<>());

        // Act
        commentNotificationConsumer.handleCommentNotification(commentCreatedMessage);

        // Assert
        verify(notificationService, never()).createBulkNotifications(anyList());
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testHandleUnknownEventType() {
        // Arrange
        CommentNotificationMessageDto unknownMessage = CommentNotificationMessageDto.builder()
                .messageId("msg-unknown")
                .eventType("UNKNOWN_EVENT")
                .commentId(1L)
                .taskId(10L)
                .authorId(100L)
                .timestamp(OffsetDateTime.now())
                .build();

        // Act
        commentNotificationConsumer.handleCommentNotification(unknownMessage);

        // Assert
        verify(notificationService, never()).createBulkNotifications(anyList());
    }

    @Test
    void testEmailNotification_WithFullUrl() {
        // Arrange
        when(taskAssigneeRepository.findAssigneeIdsByTaskId(10L))
                .thenReturn(List.of(200L));

        NotificationDto notification = createMockNotificationDto(1L, 200L, "Test notification");

        when(notificationService.createBulkNotifications(anyList()))
                .thenReturn(List.of(notification));

        when(userManagementService.getUserById(200L))
                .thenReturn(new UserResponseDto(200L, "User", "user@test.com", null, null));

        // Act
        commentNotificationConsumer.handleCommentNotification(commentCreatedMessage);

        // Assert
        ArgumentCaptor<String> emailBodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(eq("user@test.com"), anyString(), emailBodyCaptor.capture());

        String emailBody = emailBodyCaptor.getValue();
        assert emailBody.contains("http://localhost:3000/tasks/");
    }

    @Test
    void testEmailNotification_UserWithNoEmail() {
        // Arrange
        when(taskAssigneeRepository.findAssigneeIdsByTaskId(10L))
                .thenReturn(List.of(200L));

        NotificationDto notification = createMockNotificationDto(1L, 200L, "Test notification");

        when(notificationService.createBulkNotifications(anyList()))
                .thenReturn(List.of(notification));

        when(userManagementService.getUserById(200L))
                .thenReturn(new UserResponseDto(200L, "User", "", null, null));

        // Act
        commentNotificationConsumer.handleCommentNotification(commentCreatedMessage);

        // Assert - Email should not be sent for empty email address
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testEmailNotification_UserNotFound() {
        // Arrange
        when(taskAssigneeRepository.findAssigneeIdsByTaskId(10L))
                .thenReturn(List.of(200L));

        NotificationDto notification = createMockNotificationDto(1L, 200L, "Test notification");

        when(notificationService.createBulkNotifications(anyList()))
                .thenReturn(List.of(notification));

        when(userManagementService.getUserById(200L))
                .thenThrow(new RuntimeException("User not found"));

        // Act
        commentNotificationConsumer.handleCommentNotification(commentCreatedMessage);

        // Assert - Should not throw exception, email service should not be called
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testNotificationProcessing_WithException() {
        // Arrange
        when(taskAssigneeRepository.findAssigneeIdsByTaskId(10L))
                .thenReturn(List.of(200L));

        when(notificationService.createBulkNotifications(anyList()))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        try {
            commentNotificationConsumer.handleCommentNotification(commentCreatedMessage);
            assert false : "Expected exception to be thrown";
        } catch (RuntimeException e) {
            assert e.getMessage().equals("Database error");
        }
    }

    @Test
    void testCommentReply_Event() {
        // Arrange
        CommentNotificationMessageDto replyMessage = CommentNotificationMessageDto.builder()
                .messageId("msg-reply")
                .eventType("COMMENT_REPLY")
                .commentId(5L)
                .taskId(10L)
                .authorId(100L)
                .content("Replying to your comment")
                .parentCommentAuthorId(500L) // Parent comment author
                .taskTitle("Test Task")
                .timestamp(OffsetDateTime.now())
                .build();

        NotificationDto notification = createMockNotificationDto(1L, 500L, "Someone replied");

        when(notificationService.createBulkNotifications(anyList()))
                .thenReturn(List.of(notification));

        when(userManagementService.getUserById(500L))
                .thenReturn(new UserResponseDto(500L, "ParentUser", "parent@test.com", null, null));

        // Act
        commentNotificationConsumer.handleCommentNotification(replyMessage);

        // Assert
        ArgumentCaptor<List<CreateNotificationDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationService).createBulkNotifications(captor.capture());

        List<CreateNotificationDto> capturedNotifications = captor.getValue();
        assert capturedNotifications.size() == 1;
        assert capturedNotifications.get(0).getTargetId().equals(500L);

        verify(emailService).sendEmail(eq("parent@test.com"), anyString(), anyString());
    }

    private NotificationDto createMockNotificationDto(Long id, Long targetId, String subject) {
        return NotificationDto.builder()
                .notificationId(id)
                .authorId(100L)
                .targetId(targetId)
                .notificationType(NotificationType.COMMENT_REPLY)
                .subject(subject)
                .message("Test message")
                .channels(List.of(Channel.IN_APP, Channel.EMAIL))
                .readStatus(false)
                .dismissedStatus(false)
                .priority(Priority.MEDIUM)
                .link("/tasks/10/comments/" + id)
                .createdAt(Instant.now())
                .readAt(null)
                .build();
    }
}
