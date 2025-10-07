package com.spmorangle.crm.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spmorangle.common.enums.NotificationType;
import com.spmorangle.common.model.User;
import com.spmorangle.common.service.UserContextService;
import com.spmorangle.crm.notification.dto.NotificationDto;
import com.spmorangle.crm.notification.dto.NotificationFilterDto;
import com.spmorangle.crm.notification.dto.UnreadCountDto;
import com.spmorangle.crm.notification.enums.Channel;
import com.spmorangle.crm.notification.enums.Priority;
import com.spmorangle.crm.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(NotificationController.class)
@WithMockUser
@DisplayName("NotificationController Test Cases")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private UserContextService userContextService;

    private User testUser;
    private List<NotificationDto> mockNotifications;
    private UnreadCountDto unreadCountDto;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUserName("testuser");
        testUser.setEmail("test@example.com");

        // Setup mock notifications
        NotificationDto notification1 = NotificationDto.builder()
                .notificationId(1L)
                .authorId(100L)
                .targetId(1L)
                .notificationType(NotificationType.MENTION)
                .subject("You were mentioned")
                .message("Sarah mentioned you in a comment")
                .channels(Arrays.asList(Channel.IN_APP))
                .readStatus(false)
                .dismissedStatus(false)
                .priority(Priority.HIGH)
                .link("/tasks/123/comments")
                .createdAt(OffsetDateTime.now().toInstant())
                .build();

        NotificationDto notification2 = NotificationDto.builder()
                .notificationId(2L)
                .authorId(101L)
                .targetId(1L)
                .notificationType(NotificationType.TASK_ASSIGNED)
                .subject("New task assigned")
                .message("You have been assigned to 'Fix login bug'")
                .channels(Arrays.asList(Channel.IN_APP))
                .readStatus(true)
                .dismissedStatus(false)
                .priority(Priority.MEDIUM)
                .link("/tasks/456")
                .createdAt(OffsetDateTime.now().toInstant())
                .build();

        mockNotifications = Arrays.asList(notification1, notification2);
        List<NotificationDto> unreadOnlyNotifications = Arrays.asList(notification1); // Only unread notifications

        // Setup unread count (notification1 is unread, so count is 1, but test expects it to match unread list size)
        unreadCountDto = new UnreadCountDto(unreadOnlyNotifications.size());

        // Mock the userContextService to return testUser
        when(userContextService.getRequestingUser()).thenReturn(testUser);

        // Mock the notificationService responses
        // Default: all notifications
        Page<NotificationDto> allNotificationsPage = new PageImpl<>(mockNotifications, PageRequest.of(0, 20), mockNotifications.size());
        Page<NotificationDto> unreadNotificationsPage = new PageImpl<>(unreadOnlyNotifications, PageRequest.of(0, 20), unreadOnlyNotifications.size());

        // Use answer to return different pages based on filter
        when(notificationService.getNotificationsWithFilters(eq(1L), any(NotificationFilterDto.class), any()))
            .thenAnswer(invocation -> {
                NotificationFilterDto filter = invocation.getArgument(1);
                return filter.isUnreadOnly() ? unreadNotificationsPage : allNotificationsPage;
            });

        when(notificationService.getUnreadCount(eq(1L))).thenReturn(unreadCountDto);
        doNothing().when(notificationService).markAsRead(anyLong(), eq(1L));
        doNothing().when(notificationService).markAllAsRead(eq(1L));
    }

    @Test
    @DisplayName("Should get all notifications when unreadOnly is false")
    void testGetAllNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications")
                .param("unreadOnly", "false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
            .andExpect(jsonPath("$[0].notificationId").exists())
            .andExpect(jsonPath("$[0].notificationType").exists())
            .andExpect(jsonPath("$[0].subject").exists())
            .andExpect(jsonPath("$[0].message").exists());
    }

    @Test
    @DisplayName("Should get all notifications when unreadOnly parameter is not provided")
    void testGetAllNotificationsWithoutParameter() throws Exception {
        mockMvc.perform(get("/api/notifications"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("Should get only unread notifications when unreadOnly is true")
    void testGetUnreadNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications")
                .param("unreadOnly", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[*].readStatus", everyItem(is(false))));
    }

    @Test
    @DisplayName("Should verify first mock notification structure")
    void testFirstNotificationStructure() throws Exception {
        mockMvc.perform(get("/api/notifications"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].notificationId").value(1))
            .andExpect(jsonPath("$[0].authorId").value(100))
            .andExpect(jsonPath("$[0].targetId").value(1))
            .andExpect(jsonPath("$[0].notificationType").value("MENTION"))
            .andExpect(jsonPath("$[0].subject").value("You were mentioned"))
            .andExpect(jsonPath("$[0].message").value("Sarah mentioned you in a comment"))
            .andExpect(jsonPath("$[0].channels[0]").value("IN_APP"))
            .andExpect(jsonPath("$[0].readStatus").value(false))
            .andExpect(jsonPath("$[0].dismissedStatus").value(false))
            .andExpect(jsonPath("$[0].priority").value("HIGH"))
            .andExpect(jsonPath("$[0].link").value("/tasks/123/comments"))
            .andExpect(jsonPath("$[0].createdAt").exists());
    }

    @Test
    @DisplayName("Should verify second mock notification structure")
    void testSecondNotificationStructure() throws Exception {
        mockMvc.perform(get("/api/notifications"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[1].notificationId").value(2))
            .andExpect(jsonPath("$[1].authorId").value(101))
            .andExpect(jsonPath("$[1].targetId").value(1))
            .andExpect(jsonPath("$[1].notificationType").value("TASK_ASSIGNED"))
            .andExpect(jsonPath("$[1].subject").value("New task assigned"))
            .andExpect(jsonPath("$[1].message").value("You have been assigned to 'Fix login bug'"))
            .andExpect(jsonPath("$[1].readStatus").value(true))
            .andExpect(jsonPath("$[1].priority").value("MEDIUM"))
            .andExpect(jsonPath("$[1].link").value("/tasks/456"));
    }

    @Test
    @DisplayName("Should get unread count")
    void testGetUnreadCount() throws Exception {
        mockMvc.perform(get("/api/notifications/unread-count"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").exists())
            .andExpect(jsonPath("$.count").isNumber())
            .andExpect(jsonPath("$.count", greaterThanOrEqualTo(0)));
    }

    @Test
    @DisplayName("Should mark notification as read")
    void testMarkAsRead() throws Exception {
        Long notificationId = 1L;

        mockMvc.perform(patch("/api/notifications/{id}/read", notificationId)
                .with(csrf()))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should mark all notifications as read")
    void testMarkAllAsRead() throws Exception {
        mockMvc.perform(patch("/api/notifications/mark-all-read")
                .with(csrf()))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should verify unread count matches unread notifications")
    void testUnreadCountMatchesUnreadNotifications() throws Exception {
        // Get unread notifications count
        String unreadResponse = mockMvc.perform(get("/api/notifications")
                .param("unreadOnly", "true"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        List<?> unreadNotifications = objectMapper.readValue(unreadResponse, List.class);
        int unreadNotificationCount = unreadNotifications.size();

        // Get unread count from endpoint
        mockMvc.perform(get("/api/notifications/unread-count"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(unreadNotificationCount));
    }

    @Test
    @DisplayName("Should return all notifications with different read statuses")
    void testMixedReadStatuses() throws Exception {
        mockMvc.perform(get("/api/notifications"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].readStatus").value(false))
            .andExpect(jsonPath("$[1].readStatus").value(true));
    }

    @Test
    @DisplayName("Should have correct endpoint mappings")
    void testEndpointMappings() throws Exception {
        // Verify all endpoints are accessible
        mockMvc.perform(get("/api/notifications"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications/unread-count"))
            .andExpect(status().isOk());

        mockMvc.perform(patch("/api/notifications/1/read")
                .with(csrf()))
            .andExpect(status().isNoContent());

        mockMvc.perform(patch("/api/notifications/mark-all-read")
                .with(csrf()))
            .andExpect(status().isNoContent());
    }
}
