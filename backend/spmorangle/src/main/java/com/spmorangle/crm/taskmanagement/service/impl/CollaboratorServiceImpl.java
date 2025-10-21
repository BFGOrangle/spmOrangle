package com.spmorangle.crm.taskmanagement.service.impl;

import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.crm.notification.messaging.dto.TaskNotificationMessageDto;
import com.spmorangle.crm.notification.messaging.publisher.NotificationMessagePublisher;
import com.spmorangle.crm.reporting.service.ReportService;
import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorRequestDto;
import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorResponseDto;
import com.spmorangle.crm.taskmanagement.dto.RemoveCollaboratorRequestDto;
import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.model.TaskAssignee;
import com.spmorangle.crm.taskmanagement.model.TaskAssigneeCK;
import com.spmorangle.crm.taskmanagement.repository.TaskAssigneeRepository;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import com.spmorangle.crm.taskmanagement.service.CollaboratorService;
import com.spmorangle.crm.taskmanagement.service.exception.CollaboratorAlreadyExistsException;
import com.spmorangle.crm.taskmanagement.service.exception.CollaboratorAssignmentNotFoundException;
import com.spmorangle.crm.taskmanagement.service.exception.TaskNotFoundException;
import com.spmorangle.crm.taskmanagement.service.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollaboratorServiceImpl implements CollaboratorService {

    private final TaskAssigneeRepository taskAssigneeRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ReportService reportService;
    private final NotificationMessagePublisher notificationMessagePublisher;

    @Override
    public AddCollaboratorResponseDto addCollaborator(AddCollaboratorRequestDto requestDto, Long assignedById) {
        long taskId = requestDto.getTaskId();
        long collaboratorId = requestDto.getCollaboratorId();

        log.info("🔵 [COLLABORATOR] Attempting to add collaborator - TaskId: {}, CollaboratorId: {}, AssignedById: {}",
                 taskId, collaboratorId, assignedById);

        // Validate task exists
        if (!taskRepository.existsById(taskId)) {
            log.error("❌ [COLLABORATOR] Task not found - TaskId: {}", taskId);
            throw new TaskNotFoundException(taskId);
        }

        // Validate collaborator user exists
        if (!userRepository.existsById(collaboratorId)) {
            log.error("❌ [COLLABORATOR] Collaborator user not found - UserId: {}", collaboratorId);
            throw new UserNotFoundException(collaboratorId, "collaborator");
        }

        // Validate assigned_by user exists
        if (!userRepository.existsById(assignedById)) {
            log.error("❌ [COLLABORATOR] Assigned by user not found - UserId: {}", assignedById);
            throw new UserNotFoundException(assignedById, "assigned_by");
        }

        if (taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(taskId, collaboratorId, assignedById)) {
            log.warn("⚠️ [COLLABORATOR] Collaborator already exists - TaskId: {}, CollaboratorId: {}", taskId, collaboratorId);
            throw new CollaboratorAlreadyExistsException(taskId, collaboratorId);
        }

        TaskAssignee taskAssignee = new TaskAssignee();
        taskAssignee.setTaskId(taskId);
        taskAssignee.setUserId(collaboratorId);
        taskAssignee.setAssignedId(assignedById);

        log.info("💾 [COLLABORATOR] Saving collaborator to database...");
        TaskAssignee savedAssignee = taskAssigneeRepository.save(taskAssignee);
        log.info("✅ [COLLABORATOR] Successfully added collaborator - TaskId: {}, CollaboratorId: {}, AssignedAt: {}",
                 savedAssignee.getTaskId(), savedAssignee.getUserId(), savedAssignee.getAssignedAt());

        // Sync time tracking if task is IN_PROGRESS
        try {
            reportService.syncTimeTrackingOnAssigneeAdd(taskId, collaboratorId);
        } catch (Exception e) {
            log.error("❌ [COLLABORATOR] Failed to sync time tracking for new assignee - TaskId: {}, CollaboratorId: {}", 
                     taskId, collaboratorId, e);
            // Don't fail the operation if time tracking sync fails
        }

        Task task = taskRepository.getTaskById(taskId);
        publishAssigneeAddedNotification(task, collaboratorId, assignedById);

        return AddCollaboratorResponseDto.builder()
                .taskId(savedAssignee.getTaskId())
                .collaboratorId(savedAssignee.getUserId())
                .assignedById(savedAssignee.getAssignedId())
                .assignedAt(savedAssignee.getAssignedAt())
                .build();
    }

    @Override
    public void removeCollaborator(RemoveCollaboratorRequestDto requestDto, Long assignedById) {
        long taskId = requestDto.getTaskId();
        long collaboratorId = requestDto.getCollaboratorId();

        if (!taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(taskId, collaboratorId, assignedById)) {
            throw new CollaboratorAssignmentNotFoundException(taskId, collaboratorId);
        }

        taskAssigneeRepository.deleteById(new TaskAssigneeCK(taskId, collaboratorId, assignedById));

        Task task = taskRepository.getTaskById(taskId);
        publishAssigneeRemovedNotification(task, collaboratorId, assignedById);

        // Sync time tracking if task is IN_PROGRESS
        try {
            reportService.syncTimeTrackingOnAssigneeRemove(taskId, collaboratorId);
        } catch (Exception e) {
            log.error("[COLLABORATOR] Failed to sync time tracking for removed assignee - TaskId: {}, CollaboratorId: {}",
                     taskId, collaboratorId, e);
            // Don't fail the operation if time tracking sync fails
        }
    }

    @Override
    public boolean isUserTaskCollaborator(Long taskId, Long userId) {
        return taskAssigneeRepository.existsByTaskIdAndUserId(taskId, userId);
    }

    @Override
    public List<Long> getTasksForWhichUserIsCollaborator(Long userId) {
        return taskAssigneeRepository.findTaskIdsUserIsAssigneeFor(userId);
    }

    @Override
    public List<Long> getCollaboratorIdsByTaskId(Long taskId) {
        return taskAssigneeRepository.findAssigneeIdsByTaskId(taskId);
    }

    private void publishAssigneeAddedNotification(Task task, Long assigneeId, Long assignedById) {
        try {
            if (assigneeId.equals(assignedById)) {
                return;
            }

            TaskNotificationMessageDto messageDto = TaskNotificationMessageDto.forTaskAssigned(
                task.getId(),
                assignedById,
                task.getProjectId(),
                task.getTitle(),
                task.getDescription(),
                List.of(assigneeId)
            );

            notificationMessagePublisher.publishTaskNotification(messageDto);
            log.info("Published assignee added notification for task ID: {}, assignee: {}", task.getId(), assigneeId);

        } catch (Exception e) {
            log.error("Failed to publish assignee added notification for task ID: {} - Error: {}",
                     task.getId(), e.getMessage(), e);
        }
    }

    private void publishAssigneeRemovedNotification(Task task, Long assigneeId, Long removedById) {
        try {
            if (assigneeId.equals(removedById)) {
                return;
            }

            TaskNotificationMessageDto messageDto = TaskNotificationMessageDto.forTaskUnassigned(
                task.getId(),
                removedById,
                task.getProjectId(),
                task.getTitle(),
                task.getDescription(),
                List.of(assigneeId)
            );

            notificationMessagePublisher.publishTaskNotification(messageDto);
            log.info("Published assignee removed notification for task ID: {}, assignee: {}", task.getId(), assigneeId);

        } catch (Exception e) {
            log.error("Failed to publish assignee removed notification for task ID: {} - Error: {}",
                     task.getId(), e.getMessage(), e);
        }
    }
}
