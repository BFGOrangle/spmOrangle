package com.spmorangle.crm.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spmorangle.common.enums.NotificationType;
import com.spmorangle.crm.notification.dto.NotificationDto;
import com.spmorangle.crm.notification.dto.UnreadCountDto;
import com.spmorangle.crm.notification.enums.Channel;
import com.spmorangle.crm.notification.enums.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
@DisplayName("NotificationController Test Cases")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

        mockMvc.perform(patch("/api/notifications/{id}/read", notificationId))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should mark all notifications as read")
    void testMarkAllAsRead() throws Exception {
        mockMvc.perform(patch("/api/notifications/mark-all-read"))
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

        mockMvc.perform(patch("/api/notifications/1/read"))
            .andExpect(status().isNoContent());

        mockMvc.perform(patch("/api/notifications/mark-all-read"))
            .andExpect(status().isNoContent());
    }
}
