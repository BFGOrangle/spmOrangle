package com.spmorangle.crm.taskmanagement.service.impl;

import com.spmorangle.common.model.User;
import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.crm.projectmanagement.dto.ProjectResponseDto;
import com.spmorangle.crm.projectmanagement.service.ProjectService;
import com.spmorangle.crm.taskmanagement.dto.*;
import com.spmorangle.crm.taskmanagement.model.Tag;
import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.repository.TaskAssigneeRepository;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import com.spmorangle.crm.taskmanagement.service.CollaboratorService;
import com.spmorangle.crm.taskmanagement.service.SubtaskService;
import com.spmorangle.crm.taskmanagement.service.TagService;
import com.spmorangle.crm.taskmanagement.service.TaskService;
import com.spmorangle.crm.notification.messaging.publisher.NotificationMessagePublisher;
import com.spmorangle.crm.notification.messaging.dto.TaskNotificationMessageDto;
import com.spmorangle.crm.reporting.service.ReportService;
import com.spmorangle.crm.taskmanagement.enums.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final CollaboratorService collaboratorService;
    private final SubtaskService subtaskService;
    private final ProjectService projectService;
    private final TagService tagService;
    private final NotificationMessagePublisher notificationPublisher;
    private final UserRepository userRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final ReportService reportService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateTaskResponseDto createTask(CreateTaskDto createTaskDto, Long currentUserId) {
        return createTask(createTaskDto, currentUserId, currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateTaskResponseDto createTask(CreateTaskDto createTaskDto, Long taskOwnerId, Long currentUserId) {
        log.info("🔵 Creating task with title: '{}' for user: {}", createTaskDto.getTitle(), currentUserId);
        log.info("📋 CreateTaskDto details - ProjectId: {}, OwnerId: {}, AssignedUserIds: {}", 
                 createTaskDto.getProjectId(), createTaskDto.getOwnerId(), createTaskDto.getAssignedUserIds());
        
        Task task = new Task();
        
        task.setProjectId(createTaskDto.getProjectId());
        task.setOwnerId(taskOwnerId);
        task.setTaskType(createTaskDto.getTaskType());
        task.setTitle(createTaskDto.getTitle());
        task.setDescription(createTaskDto.getDescription());
        task.setStatus(createTaskDto.getStatus());
        
        // Convert tag strings to Tag entities
        if (createTaskDto.getTags() != null && !createTaskDto.getTags().isEmpty()) {
            task.setTags(tagService.findOrCreateTags(createTaskDto.getTags()));
        }
        
        task.setCreatedBy(taskOwnerId);
        task.setCreatedAt(OffsetDateTime.now());
        task.setDueDateTime(createTaskDto.getDueDateTime());
        
        Task savedTask = taskRepository.save(task);
        log.info("✅ Task created with ID: {}", savedTask.getId());
        
        List<Long> assignedUserIds = new ArrayList<>();
        if (createTaskDto.getAssignedUserIds() != null && !createTaskDto.getAssignedUserIds().isEmpty()) {
            log.info("👥 Assigning task to {} users: {}", createTaskDto.getAssignedUserIds().size(), createTaskDto.getAssignedUserIds());
            for (Long userId : createTaskDto.getAssignedUserIds()) {
                try {
                    log.info("🔄 Attempting to assign task {} to user {}", savedTask.getId(), userId);
                    AddCollaboratorRequestDto collaboratorRequest = AddCollaboratorRequestDto.builder()
                            .taskId(savedTask.getId())
                            .collaboratorId(userId)
                            .build();

                    collaboratorService.addCollaborator(collaboratorRequest, currentUserId);
                    assignedUserIds.add(userId);
                    log.info("✅ Task {} successfully assigned to user: {}", savedTask.getId(), userId);
                } catch (Exception e) {
                    log.error("❌ Failed to assign task {} to user {}: {}", savedTask.getId(), userId, e.getMessage(), e);
                }
            }
        } else {
            log.info("⚠️ No users to assign - assignedUserIds is null or empty");
        }
        
        // Publish task creation notification
        try {
            if (!assignedUserIds.isEmpty()) {
                TaskNotificationMessageDto message = TaskNotificationMessageDto.forTaskCreated(
                    savedTask.getId(),
                    taskOwnerId,
                    savedTask.getProjectId(),
                    savedTask.getTitle(),
                    savedTask.getDescription(),
                    assignedUserIds
                );
                
                notificationPublisher.publishTaskNotification(message);
                log.info("Published task creation notification for task ID: {}", savedTask.getId());
            }
        } catch (Exception e) {
            log.error("Failed to publish task creation notification for task ID: {} - Error: {}",
                     savedTask.getId(), e.getMessage(), e);
            // Don't fail task creation if notification publishing fails
        }
        
        return CreateTaskResponseDto.builder()
                .id(savedTask.getId())
                .projectId(savedTask.getProjectId())
                .ownerId(savedTask.getOwnerId())
                .taskType(savedTask.getTaskType())
                .title(savedTask.getTitle())
                .description(savedTask.getDescription())
                .status(savedTask.getStatus())
                .assignedUserIds(assignedUserIds)
                .tags(savedTask.getTags() != null 
                        ? savedTask.getTags().stream().map(Tag::getTagName).toList()
                        : null)
                .userHasEditAccess(true) // Creator always has edit access
                .userHasDeleteAccess(canUserDeleteTask(savedTask.getId(), currentUserId))
                .createdBy(savedTask.getCreatedBy())
                .createdAt(savedTask.getCreatedAt())
                .dueDateTime(savedTask.getDueDateTime())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getProjectTasks(Long userId, Long projectId) {
        log.info("Getting tasks for project: {}", projectId);
        List<Task> tasks = taskRepository.findByProjectIdAndNotDeleted(projectId);
        Set<Long> tasksUserIsCollaboratorFor = new HashSet<>(collaboratorService.getTasksForWhichUserIsCollaborator(userId));
        Long projectOwnerId = projectService.getOwnerId(projectId);

        Map<Long, String> projectNames = resolveProjectNames(Collections.singleton(projectId));
        Map<Long, User> ownerDetails = resolveOwnerDetails(tasks);

        return tasks.stream()
                .map((task) -> {
                    boolean userHasWriteAccess = task.getOwnerId().equals(userId) || tasksUserIsCollaboratorFor.contains(task.getId());
                    boolean userHasDeleteAccess = userId.equals(projectOwnerId);
                    return mapToTaskResponseDto(task, userHasWriteAccess, userHasDeleteAccess, projectNames, ownerDetails, userId);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDto getTaskById(Long taskId, Long currentUserId) {
        log.info("Getting task by ID: {}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        boolean userHasEditAccess = canUserUpdateTask(taskId, currentUserId);
        boolean userHasDeleteAccess = canUserDeleteTask(taskId, currentUserId);

        Map<Long, String> projectNames = task.getProjectId() != null
            ? resolveProjectNames(Collections.singleton(task.getProjectId()))
            : Collections.emptyMap();
        Map<Long, User> ownerDetails = resolveOwnerDetails(Collections.singletonList(task));

        return mapToTaskResponseDto(task, userHasEditAccess, userHasDeleteAccess, projectNames, ownerDetails, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getPersonalTasks(Long userId) {
        log.info("Getting personal tasks for user: {}", userId);
        List<Task> tasks = taskRepository.findPersonalTasksByOwnerIdAndNotDeleted(userId);
        Map<Long, User> ownerDetails = resolveOwnerDetails(tasks);
        return tasks.stream()
                .map(task -> mapToTaskResponseDto(task, true, true, Collections.emptyMap(), ownerDetails, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getAllUserTasks(Long userId) {
        log.info("Getting all tasks for user: {}", userId);
        List<Task> tasks = taskRepository.findUserTasks(userId);

        Set<Long> projectIds = tasks.stream()
                .map(Task::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Long> projectOwnerMap = projectService.getProjectOwners(projectIds);
        Map<Long, String> projectNames = resolveProjectNames(projectIds);
        Map<Long, User> ownerDetails = resolveOwnerDetails(tasks);

        return tasks.stream()
                .map(task -> {
                    boolean userHasDeleteAccess = task.getProjectId() != null
                        && userId.equals(projectOwnerMap.get(task.getProjectId()));
                    return mapToTaskResponseDto(task, true, userHasDeleteAccess, projectNames, ownerDetails, userId);
                })
                .collect(Collectors.toList());
    }
    /*
    * This function serves to show tasks that users have members from their department as collaborators in tasks
    * in another project
    * */
    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getRelatedTasks(Long userId){
        log.info("Getting related tasks for user: {}", userId);

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String department = currentUser.getDepartment();
        if (department == null || department.isBlank()) {
            log.warn("User {} does not have a department assigned; skipping related tasks lookup", userId);
            return Collections.emptyList();
        }

        List<User> departmentMembers = userRepository.findByDepartmentIgnoreCase(department).stream()
                .filter(user -> !Objects.equals(user.getId(), userId))
                .toList();

        if (departmentMembers.isEmpty()) {
            log.info("No department members found for user {}", userId);
            return Collections.emptyList();
        }

        Set<Long> departmentMemberIds = departmentMembers.stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        List<Task> ownedTasks = taskRepository.findByOwnerIdAndNotDeleted(userId);
        Set<Long> excludedTaskIds = ownedTasks.stream()
                .map(Task::getId)
                .collect(Collectors.toCollection(HashSet::new));

        List<Long> collaboratorTaskIds = collaboratorService.getTasksForWhichUserIsCollaborator(userId);
        excludedTaskIds.addAll(collaboratorTaskIds);

        Set<Long> userProjectIds = ownedTasks.stream()
                .map(Task::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));

        if (!collaboratorTaskIds.isEmpty()) {
            taskRepository.findAllById(collaboratorTaskIds).stream()
                    .map(Task::getProjectId)
                    .filter(Objects::nonNull)
                    .forEach(userProjectIds::add);
        }

        Set<Long> relatedTaskIds = new HashSet<>();
        for (Long colleagueId : departmentMemberIds) {
            List<Long> colleagueTaskIds = taskAssigneeRepository.findTaskIdsUserIsAssigneeFor(colleagueId);
            for (Long taskId : colleagueTaskIds) {
                if (!excludedTaskIds.contains(taskId)) {
                    relatedTaskIds.add(taskId);
                }
            }
        }

        if (relatedTaskIds.isEmpty()) {
            log.info("No related task ids discovered for user {}", userId);
            return Collections.emptyList();
        }

        List<Task> relatedTasks = taskRepository.findAllById(relatedTaskIds).stream()
                .filter(task -> !task.isDeleteInd())
                .filter(task -> task.getProjectId() != null)
                .filter(task -> !userProjectIds.contains(task.getProjectId()))
                .collect(Collectors.toList());

        if (relatedTasks.isEmpty()) {
            log.info("No related tasks remained after filtering for user {}", userId);
            return Collections.emptyList();
        }

        Set<Long> projectIds = relatedTasks.stream()
                .map(Task::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Long> projectOwnerMap = projectIds.isEmpty()
                ? Collections.emptyMap()
                : projectService.getProjectOwners(projectIds);

        relatedTasks = relatedTasks.stream()
                .filter(task -> !Objects.equals(projectOwnerMap.get(task.getProjectId()), userId))
                .collect(Collectors.toList());

        if (relatedTasks.isEmpty()) {
            log.info("All candidate related tasks belonged to user's own projects: {}", userId);
            return Collections.emptyList();
        }

        relatedTasks.sort((left, right) -> {
            OffsetDateTime leftTime = Optional.ofNullable(left.getUpdatedAt()).orElse(left.getCreatedAt());
            OffsetDateTime rightTime = Optional.ofNullable(right.getUpdatedAt()).orElse(right.getCreatedAt());

            if (leftTime == null && rightTime == null) {
                return 0;
            }
            if (leftTime == null) {
                return 1;
            }
            if (rightTime == null) {
                return -1;
            }
            return rightTime.compareTo(leftTime);
        });

        Map<Long, String> projectNames = resolveProjectNames(projectIds);
        Map<Long, User> ownerDetails = resolveOwnerDetails(relatedTasks);

        return relatedTasks.stream()
                .map(task -> mapToTaskResponseDto(task, false, false, projectNames, ownerDetails, userId))
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public UpdateTaskResponseDto updateTask(UpdateTaskDto updateTaskDto, Long currentUserId) {
        log.info("Updating task: {} by user: {}", updateTaskDto.getTaskId(), currentUserId);

        Task task = taskRepository.findById(updateTaskDto.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // Only owner or collaborators can update the task
        if (!canUserUpdateTask(updateTaskDto.getTaskId(), currentUserId)) {
            throw new RuntimeException("Only task owner or collaborators can update the task");
        }

        // Update only fields that are present in the DTO
        if (updateTaskDto.getTitle() != null) {
            task.setTitle(updateTaskDto.getTitle());
        }

        if (updateTaskDto.getDescription() != null) {
            task.setDescription(updateTaskDto.getDescription());
        }

        if (updateTaskDto.getStatus() != null) {
            Status oldStatus = task.getStatus();
            Status newStatus = updateTaskDto.getStatus();
            task.setStatus(newStatus);
            
            // Handle time tracking for status transitions
            handleTimeTracking(task.getId(), currentUserId, oldStatus, newStatus);
        }

        if (updateTaskDto.getTaskType() != null) {
            task.setTaskType(updateTaskDto.getTaskType());
        }

        if (updateTaskDto.getTags() != null) {
            task.getTags().clear();
            task.getTags().addAll(tagService.findOrCreateTags(updateTaskDto.getTags()));
        }

        task.setDueDateTime(updateTaskDto.getDueDateTime());
        task.setUpdatedBy(currentUserId);
        task.setUpdatedAt(OffsetDateTime.now());

        Task updatedTask = taskRepository.save(task);
        log.info("Task {} updated successfully", updatedTask.getId());

        return UpdateTaskResponseDto.builder()
                .id(updatedTask.getId())
                .projectId(updatedTask.getProjectId())
                .ownerId(updatedTask.getOwnerId())
                .taskType(updatedTask.getTaskType())
                .title(updatedTask.getTitle())
                .description(updatedTask.getDescription())
                .status(updatedTask.getStatus())
                                .status(updatedTask.getStatus())
                .tags(updatedTask.getTags() != null 
                        ? updatedTask.getTags().stream().map(Tag::getTagName).toList()
                        : null)
                .userHasEditAccess(true) // User who just updated has edit access
                .userHasDeleteAccess(canUserDeleteTask(updatedTask.getId(), currentUserId))
                .updatedAt(updatedTask.getUpdatedAt())
                .updatedBy(updatedTask.getUpdatedBy())
                .dueDateTime(updatedTask.getDueDateTime())
                .build();
    }

    // only for managers
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long taskId, Long currentUserId) {
        log.info("Deleting task: {} by user: {}", taskId, currentUserId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // Only manager of the project can delete the task
        if (!canUserDeleteTask(taskId, currentUserId)) {
            throw new RuntimeException("Only project owner or collaborators can delete the task");
        }

        task.setDeleteInd(true);
        task.setUpdatedBy(currentUserId);
        task.setUpdatedAt(OffsetDateTime.now());

        taskRepository.save(task);
        log.info("Task {} marked as deleted", taskId);
    }

    @Override
    public boolean canUserUpdateTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        if (task.getOwnerId().equals(userId)) {
            return true;
        }
        return collaboratorService.isUserTaskCollaborator(taskId, userId);
    }

    @Override
    public boolean canUserDeleteTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Long projectId = task.getProjectId();
        
        if (projectId == null) {
            return task.getOwnerId().equals(userId);
        }
        
        Long projectOwnerId = projectService.getOwnerId(projectId);
        return projectOwnerId.equals(userId);
    }



    private Map<Long, String> resolveProjectNames(Set<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return projectService.getProjectsByIds(projectIds).stream()
                .collect(Collectors.toMap(ProjectResponseDto::getId, ProjectResponseDto::getName));
    }

    private Map<Long, User> resolveOwnerDetails(Collection<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> ownerIds = tasks.stream()
                .map(Task::getOwnerId)
                .collect(Collectors.toSet());

        if (ownerIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return userRepository.findAllById(ownerIds).stream()
                .collect(Collectors.toMap(User::getId, owner -> owner));
    }

    private TaskResponseDto mapToTaskResponseDto(
            Task task,
            boolean userHasEditAccess,
            boolean userHasDeleteAccess,
            Map<Long, String> projectNames,
            Map<Long, User> ownerDetails,
            Long userId) {
        // Load subtasks for this task
        List<SubtaskResponseDto> subtasks = subtaskService.getSubtasksByTaskId(task.getId(), userId);

        // Load collaborator IDs for this task
        List<Long> assignedUserIds = collaboratorService.getCollaboratorIdsByTaskId(task.getId());

        User owner = ownerDetails != null ? ownerDetails.get(task.getOwnerId()) : null;
        String projectName = null;
        if (projectNames != null && task.getProjectId() != null) {
            projectName = projectNames.get(task.getProjectId());
        }

        return TaskResponseDto.builder()
                .id(task.getId())
                .projectId(task.getProjectId())
                .projectName(projectName)
                .ownerId(task.getOwnerId())
                .ownerName(owner != null ? owner.getUserName() : null)
                .ownerDepartment(owner != null ? owner.getDepartment() : null)
                .taskType(task.getTaskType())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .tags(task.getTags() != null
                        ? task.getTags().stream().map(Tag::getTagName).toList()
                        : null)
                .assignedUserIds(assignedUserIds)
                .userHasEditAccess(userHasEditAccess)
                .userHasDeleteAccess(userHasDeleteAccess)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .createdBy(task.getCreatedBy())
                .updatedBy(task.getUpdatedBy())
                .dueDateTime(task.getDueDateTime())
                .subtasks(subtasks)
                .build();
    }
    
    /**
     * Handle time tracking when task status changes
     */
    private void handleTimeTracking(Long taskId, Long userId, Status oldStatus, Status newStatus) {
        try {
            // Start time tracking when moving from TODO to IN_PROGRESS
            if (oldStatus == Status.TODO && newStatus == Status.IN_PROGRESS) {
                log.info("Starting time tracking for task: {} by user: {}", taskId, userId);
                reportService.startTimeTracking(taskId, userId);
            }
            // End time tracking when moving to COMPLETED
            else if (newStatus == Status.COMPLETED && oldStatus != Status.COMPLETED) {
                log.info("Ending time tracking for task: {} by user: {}", taskId, userId);
                reportService.endTimeTracking(taskId, userId);
            }
            // Restart time tracking if moving back to IN_PROGRESS from COMPLETED
            else if (oldStatus == Status.COMPLETED && newStatus == Status.IN_PROGRESS) {
                log.info("Restarting time tracking for task: {} by user: {}", taskId, userId);
                reportService.startTimeTracking(taskId, userId);
            }
        } catch (Exception e) {
            log.error("Error handling time tracking for task: {} by user: {}", taskId, userId, e);
            // Don't fail the task update if time tracking fails
        }
    }

}
