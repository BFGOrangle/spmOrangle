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
import com.spmorangle.crm.taskmanagement.service.exception.MaxAssigneesExceededException;
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
    private final com.spmorangle.crm.projectmanagement.service.ProjectService projectService;

    @Override
    public AddCollaboratorResponseDto addCollaborator(AddCollaboratorRequestDto requestDto, Long assignedById) {
        long taskId = requestDto.getTaskId();
        long collaboratorId = requestDto.getCollaboratorId();

        log.info("🔵 [COLLABORATOR] Attempting to add collaborator - TaskId: {}, CollaboratorId: {}, AssignedById: {}",
                 taskId, collaboratorId, assignedById);

        // Validate task exists
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("❌ [COLLABORATOR] Task not found - TaskId: {}", taskId);
                    return new TaskNotFoundException(taskId);
                });

        // Authorization check: User must be a project member to add collaborators
        if (!projectService.isUserProjectMember(assignedById, task.getProjectId())) {
            log.error("❌ [COLLABORATOR] User {} is not a member of project {}", assignedById, task.getProjectId());
            throw new com.spmorangle.crm.projectmanagement.exception.ForbiddenException(
                "Cannot add collaborators to tasks in a view-only project. You must be a project member or owner.");
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

        // Check if adding this collaborator would exceed the maximum limit
        int currentAssigneeCount = taskAssigneeRepository.countByTaskId(taskId);
        if (currentAssigneeCount >= MaxAssigneesExceededException.getMaxAssignees()) {
            log.error("❌ [COLLABORATOR] Max assignees exceeded - TaskId: {}, CurrentCount: {}", taskId, currentAssigneeCount);
            throw new MaxAssigneesExceededException(taskId, currentAssigneeCount);
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

        // Get task for authorization check
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        // Authorization check: User must be a project member to remove collaborators
        if (!projectService.isUserProjectMember(assignedById, task.getProjectId())) {
            throw new com.spmorangle.crm.projectmanagement.exception.ForbiddenException(
                "Cannot remove collaborators from tasks in a view-only project. You must be a project member or owner.");
        }

        if (!taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(taskId, collaboratorId, assignedById)) {
            throw new CollaboratorAssignmentNotFoundException(taskId, collaboratorId);
        }

        taskAssigneeRepository.deleteById(new TaskAssigneeCK(taskId, collaboratorId, assignedById));

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
