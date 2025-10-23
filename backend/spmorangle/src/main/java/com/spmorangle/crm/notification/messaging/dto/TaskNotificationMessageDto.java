package com.spmorangle.crm.notification.messaging.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Message DTO for task-related notifications sent via RabbitMQ
 */
@Slf4j
@Data
@NoArgsConstructor
@Builder
public class TaskNotificationMessageDto {

    private String messageId;
    private String eventType; // TASK_CREATED, TASK_ASSIGNED, TASK_UPDATED, TASK_COMPLETED
    private Long taskId;
    private Long authorId; // User who triggered the action
    private Long projectId;
    private String taskTitle;
    private String taskDescription;
    private List<Long> assignedUserIds; // Users assigned to the task
    private String prevTaskStatus;
    private String taskStatus;
    private Instant timestamp;

    @JsonCreator
    public TaskNotificationMessageDto(
            @JsonProperty("messageId") String messageId,
            @JsonProperty("eventType") String eventType,
            @JsonProperty("taskId") Long taskId,
            @JsonProperty("authorId") Long authorId,
            @JsonProperty("projectId") Long projectId,
            @JsonProperty("taskTitle") String taskTitle,
            @JsonProperty("taskDescription") String taskDescription,
            @JsonProperty("assignedUserIds") List<Long> assignedUserIds,
            @JsonProperty("prevTaskStatus") String prevTaskStatus,
            @JsonProperty("taskStatus") String taskStatus,
            @JsonProperty("timestamp") Instant timestamp) {
        this.messageId = messageId;
        this.eventType = eventType;
        this.taskId = taskId;
        this.authorId = authorId;
        this.projectId = projectId;
        this.taskTitle = taskTitle;
        this.taskDescription = taskDescription;
        this.assignedUserIds = assignedUserIds;
        this.prevTaskStatus = prevTaskStatus;
        this.taskStatus = taskStatus;
        this.timestamp = timestamp;
    }

    // Factory method for task creation
    public static TaskNotificationMessageDto forTaskCreated(
            Long taskId,
            Long authorId,
            Long projectId,
            String taskTitle,
            String taskDescription,
            List<Long> assignedUserIds) {
        
        return TaskNotificationMessageDto.builder()
                .messageId(UUID.randomUUID().toString())
                .eventType("TASK_CREATED")
                .taskId(taskId)
                .authorId(authorId)
                .projectId(projectId)
                .taskTitle(taskTitle)
                .taskDescription(taskDescription)
                .assignedUserIds(assignedUserIds)
                .timestamp(Instant.now())
                .build();
    }

    // Factory method for task assignment
    public static TaskNotificationMessageDto forTaskAssigned(
            Long taskId,
            Long authorId,
            Long projectId,
            String taskTitle,
            String taskDescription,
            List<Long> assignedUserIds
            ) {

        return TaskNotificationMessageDto.builder()
                .messageId(UUID.randomUUID().toString())
                .eventType("TASK_ASSIGNED")
                .taskId(taskId)
                .authorId(authorId)
                .projectId(projectId)
                .taskTitle(taskTitle)
                .taskDescription(taskDescription)
                .assignedUserIds(assignedUserIds)
                .timestamp(Instant.now())
                .build();
    }

    public static TaskNotificationMessageDto forTaskUnassigned(
            Long taskId,
            Long authorId,
            Long projectId,
            String taskTitle,
            String taskDescription,
            List<Long> removedUserIds
            ) {

        return TaskNotificationMessageDto.builder()
                .messageId(UUID.randomUUID().toString())
                .eventType("TASK_UNASSIGNED")
                .taskId(taskId)
                .authorId(authorId)
                .projectId(projectId)
                .taskTitle(taskTitle)
                .taskDescription(taskDescription)
                .assignedUserIds(removedUserIds)
                .timestamp(Instant.now())
                .build();
    }

    // Factory method for task completion
    public static TaskNotificationMessageDto forTaskCompleted(
            Long taskId,
            Long authorId,
            Long projectId,
            String taskTitle,
            List<Long> assignedUserIds) {
        
        return TaskNotificationMessageDto.builder()
                .messageId(UUID.randomUUID().toString())
                .eventType("TASK_COMPLETED")
                .taskId(taskId)
                .authorId(authorId)
                .projectId(projectId)
                .taskTitle(taskTitle)
                .assignedUserIds(assignedUserIds)
                .timestamp(Instant.now())
                .build();
    }

    // Factory method for task update
    public static TaskNotificationMessageDto forTaskUpdated(
            Long taskId,
            Long authorId,
            Long projectId,
            String taskTitle,
            String taskStatus,
            List<Long> assignedUserIds) {
        
        return TaskNotificationMessageDto.builder()
                .messageId(UUID.randomUUID().toString())
                .eventType("TASK_UPDATED")
                .taskId(taskId)
                .authorId(authorId)
                .projectId(projectId)
                .taskTitle(taskTitle)
                .taskStatus(taskStatus)
                .assignedUserIds(assignedUserIds)
                .timestamp(Instant.now())
                .build();
    }

    // Factory method for status change
    public static TaskNotificationMessageDto forStatusChange(
        Long taskId,
        Long editorId,
        Long projectId,
        String taskTitle,
        String previousStatus,
        String newStatus,
        List<Long> recipientIds
    ) {
        return TaskNotificationMessageDto.builder()
                .messageId(UUID.randomUUID().toString())
                .eventType("STATUS_UPDATED")
                .taskId(taskId)
                .authorId(editorId)
                .projectId(projectId)
                .taskTitle(taskTitle)
                .prevTaskStatus(previousStatus)
                .taskStatus(newStatus)
                .assignedUserIds(recipientIds)
                .timestamp(Instant.now())
                .build();
    }

    // Helper methods
    public boolean hasAssignees() {
        return assignedUserIds != null && !assignedUserIds.isEmpty();
    }

    public String getTaskSnippet(int maxLength) {
        if (taskDescription == null || taskDescription.isEmpty()) {
            return "";
        }
        return taskDescription.length() > maxLength 
            ? taskDescription.substring(0, maxLength) + "..." 
            : taskDescription;
    }

    public String generateNotificationLink() {
        return generateNotificationLinkWithContext(null);
    }

    public String generateNotificationLinkWithContext(String highlightSection) {
        if (highlightSection != null && !highlightSection.isEmpty()) {
            return String.format("/tasks/%d?highlight=%s", taskId, highlightSection);
        }
        return String.format("/tasks/%d", taskId);
    }

    public String getRoutingKey() {
        switch (eventType) {
            case "TASK_CREATED":
                return "notification.task.created";
            case "TASK_ASSIGNED":
                return "notification.task.assigned";
            case "TASK_COMPLETED":
                return "notification.task.completed";
            case "TASK_UPDATED":
                return "notification.task.updated";
            case "STATUS_UPDATED":
                return "notification.task.status.updated";
            default:
                log.warn("Unknown event type: {}, using default routing key", eventType);
                return "notification.task.created";
        }
    }
}
