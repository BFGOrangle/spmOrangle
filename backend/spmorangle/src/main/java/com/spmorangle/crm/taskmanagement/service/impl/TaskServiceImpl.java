package com.spmorangle.crm.taskmanagement.service.impl;

import com.spmorangle.common.model.User;
import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.crm.projectmanagement.dto.ProjectResponseDto;
import com.spmorangle.crm.projectmanagement.service.ProjectService;
import com.spmorangle.crm.taskmanagement.dto.*;
import com.spmorangle.crm.taskmanagement.enums.RecurrenceEditMode;
import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.enums.TaskType;
import com.spmorangle.crm.taskmanagement.model.Tag;
import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.repository.TaskAssigneeRepository;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import com.spmorangle.crm.taskmanagement.service.*;
import com.spmorangle.crm.notification.messaging.publisher.NotificationMessagePublisher;
import com.spmorangle.crm.notification.messaging.dto.TaskNotificationMessageDto;
import com.spmorangle.crm.reporting.service.ReportService;
import com.spmorangle.crm.taskmanagement.enums.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
    private final RecurrenceService recurrenceService;
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

        // Set recurrence fields
        task.setIsRecurring(createTaskDto.getIsRecurring());
        task.setRecurrenceRuleStr(createTaskDto.getRecurrenceRuleStr());
        task.setStartDate(createTaskDto.getStartDate());
        task.setEndDate(createTaskDto.getEndDate());

        // Validate recurrence configuration
        validateRecurrence(
            createTaskDto.getIsRecurring(),
            createTaskDto.getRecurrenceRuleStr(),
            createTaskDto.getStartDate(),
            createTaskDto.getEndDate()
        );

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
                .isRecurring(savedTask.getIsRecurring())
                .recurrenceRuleStr(savedTask.getRecurrenceRuleStr())
                .startDate(savedTask.getStartDate())
                .endDate(savedTask.getEndDate())
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

        // Only MANAGER role should see related tasks
        String userRole = currentUser.getRoleType();
        if (!"MANAGER".equalsIgnoreCase(userRole)) {
            log.info("User {} with role {} is not a MANAGER; skipping related tasks lookup", userId, userRole);
            return Collections.emptyList();
        }

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

        // Handle time tracking for status transitions (before any updates)
        if (updateTaskDto.getStatus() != null) {
            Status oldStatus = task.getStatus();
            Status newStatus = updateTaskDto.getStatus();
            handleTimeTracking(task.getId(), currentUserId, oldStatus, newStatus);

            // If there is a status change
            if(oldStatus != newStatus) {
                publishStatusChangeNotification(task, oldStatus, newStatus, currentUserId);
            }
        }

        // Handle recurring task edits method
        if(updateTaskDto.getRecurrenceEditMode() != null) {
            // Update status for recurring task edits if needed
            if (updateTaskDto.getStatus() != null) {
                task.setStatus(updateTaskDto.getStatus());
            }
            handleRecurringTaskEdit(task, updateTaskDto, currentUserId);
        }

        // Generate next instance of task if marked completed
        else if (updateTaskDto.getStatus() == Status.COMPLETED && Boolean.TRUE.equals(task.getIsRecurring())) {
            // Apply all field updates to current task before creating next instance
            applyFieldUpdates(task, updateTaskDto, currentUserId);

            // Check if task has recurrence rule and required dates configured
            if (task.getRecurrenceRuleStr() != null && task.getDueDateTime() != null && task.getStartDate() != null && task.getEndDate() != null) {
                try {
                    // Calculate next occurrence starting from day after current due date
                    OffsetDateTime nextStart = task.getDueDateTime().plusDays(1);
                    List<OffsetDateTime> nextOccurrences = recurrenceService.generateOccurrence(
                        task.getRecurrenceRuleStr(),
                        nextStart,
                        task.getEndDate()
                    );

                    // Only create next task if there are future occurrences
                    if (!nextOccurrences.isEmpty()) {
                        // Get the first future occurrence
                        OffsetDateTime nextDueDate = nextOccurrences.get(0);

                        // Prep inputs for DTO
                        List<String> tagNames = task.getTags() != null
                                ? task.getTags().stream().map(Tag::getTagName).toList()
                                : null;

                        // Get assigned user IDs from current task to replicate collaborators
                        List<Long> assignedUserIds = collaboratorService.getCollaboratorIdsByTaskId(task.getId());

                        // Create next task with same properties
                        CreateTaskDto recurringTaskDto = new CreateTaskDto(
                            task.getProjectId(),
                            task.getOwnerId(),
                            task.getTitle(),
                            task.getDescription(),
                            Status.TODO,
                            task.getTaskType(),
                            tagNames,
                            assignedUserIds,
                            nextDueDate,
                            task.getIsRecurring(),
                            task.getRecurrenceRuleStr(),
                            task.getStartDate(),
                            task.getEndDate()
                        );

                        this.createTask(recurringTaskDto, task.getOwnerId(), currentUserId);
                        log.info("Created recurring task instance with due date: {} for task: {}", nextDueDate, task.getId());
                    } else {
                        log.info("No more occurrences for recurring task: {}", task.getId());
                    }
                } catch (Exception e) {
                    log.error("Failed to create next recurring task instance for task {}: {}", task.getId(), e.getMessage(), e);
                    // Don't fail the update if recurring task creation fails
                }
            }
        }

        else {
            // Regular task update - apply all field updates
            applyFieldUpdates(task, updateTaskDto, currentUserId);
        }

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
                .tags(updatedTask.getTags() != null
                        ? updatedTask.getTags().stream().map(Tag::getTagName).toList()
                        : null)
                .userHasEditAccess(true) // User who just updated has edit access
                .userHasDeleteAccess(canUserDeleteTask(updatedTask.getId(), currentUserId))
                .updatedAt(updatedTask.getUpdatedAt())
                .updatedBy(updatedTask.getUpdatedBy())
                .dueDateTime(updatedTask.getDueDateTime())
                .isRecurring(updatedTask.getIsRecurring())
                .recurrenceRuleStr(updatedTask.getRecurrenceRuleStr())
                .startDate(updatedTask.getStartDate())
                .endDate(updatedTask.getEndDate())
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

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getUserTasksDueTmr(Long userId, OffsetDateTime startOfDay, OffsetDateTime endOfDay) {
        List<Task> tasks = taskRepository.findUserIncompleteTasksDueTmr(userId, startOfDay, endOfDay);

        if(tasks.isEmpty()) {
            return Collections.emptyList();
        }

        // Fetch project names
        Set<Long> projectIds = tasks.stream()
            .map(Task::getProjectId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
         Map<Long, String> projectNames = resolveProjectNames(projectIds);

        Map<Long, User> ownerDetails = resolveOwnerDetails(tasks);

        // Convert to DTOs
        return tasks.stream()
            .map(task -> mapToTaskResponseDto(
                task,
                false, // userHasEditAccess - not needed for digest
                false, // userHasDeleteAccess - not needed for digest
                projectNames,
                ownerDetails,
                userId
            ))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getUserTasksDueTomorrowForDigest(Long userId, OffsetDateTime startOfDay, OffsetDateTime endOfDay) {
        List<Task> tasks = taskRepository.findUserIncompleteTasksDueTmr(userId, startOfDay, endOfDay);

        if(tasks.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> projectIds = tasks.stream()
            .map(Task::getProjectId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Long, String> projectNames = resolveProjectNames(projectIds);

        Map<Long, User> ownerDetails = resolveOwnerDetails(tasks);

        return tasks.stream()
            .map(task -> mapToTaskResponseDtoLightweight(task, projectNames, ownerDetails))
            .collect(Collectors.toList());
    }

    private TaskResponseDto mapToTaskResponseDtoLightweight(
            Task task,
            Map<Long, String> projectNames,
            Map<Long, User> ownerDetails) {

        List<Long> assignedUserIds = collaboratorService.getCollaboratorIdsByTaskId(task.getId());

        User owner = ownerDetails.get(task.getOwnerId());
        String projectName = task.getProjectId() != null ? projectNames.get(task.getProjectId()) : null;

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
                .userHasEditAccess(false)
                .userHasDeleteAccess(false)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .createdBy(task.getCreatedBy())
                .updatedBy(task.getUpdatedBy())
                .dueDateTime(task.getDueDateTime())
                .subtasks(Collections.emptyList())
                .isRecurring(task.getIsRecurring())
                .recurrenceRuleStr(task.getRecurrenceRuleStr())
                .startDate(task.getStartDate())
                .endDate(task.getEndDate())
                .build();
    }

    /**
     * Validates recurrence configuration for a task
     * @param isRecurring Whether the task is recurring
     * @param recurrenceRuleStr The RRULE string
     * @param startDate The start date
     * @param endDate The end date
     * @throws RuntimeException if validation fails
     */
    private void validateRecurrence(Boolean isRecurring, String recurrenceRuleStr, OffsetDateTime startDate, OffsetDateTime endDate) {
        if (Boolean.TRUE.equals(isRecurring)) {
            if (recurrenceRuleStr == null || recurrenceRuleStr.trim().isEmpty()) {
                throw new RuntimeException("Recurrence rule is required when task is marked as recurring");
            }

            if (startDate == null) {
                throw new RuntimeException("Start date is required for recurring tasks");
            }

            if (endDate == null) {
                throw new RuntimeException("End date is required for recurring tasks");
            }

            if (!endDate.isAfter(startDate)) {
                throw new RuntimeException("End date must be after start date for recurring tasks");
            }

            // Validate RRULE format by attempting to generate occurrences
            try {
                recurrenceService.generateOccurrence(recurrenceRuleStr, startDate, endDate);
            } catch (Exception e) {
                throw new RuntimeException("Invalid recurrence rule format: " + e.getMessage());
            }
        }
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
                .isRecurring(task.getIsRecurring())
                .recurrenceRuleStr(task.getRecurrenceRuleStr())
                .startDate(task.getStartDate())
                .endDate(task.getEndDate())
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

    /**
     * Apply field updates from UpdateTaskDto to Task entity
     * Used across all update branches to ensure consistency
     */
    private void applyFieldUpdates(Task task, UpdateTaskDto updateTaskDto, Long currentUserId) {
        // Update only fields that are present in the DTO
        if (updateTaskDto.getTitle() != null) {
            task.setTitle(updateTaskDto.getTitle());
        }

        if (updateTaskDto.getDescription() != null) {
            task.setDescription(updateTaskDto.getDescription());
        }

        if (updateTaskDto.getStatus() != null) {
            task.setStatus(updateTaskDto.getStatus());
        }

        if (updateTaskDto.getTaskType() != null) {
            task.setTaskType(updateTaskDto.getTaskType());
        }

        if (updateTaskDto.getTags() != null) {
            task.getTags().clear();
            task.getTags().addAll(tagService.findOrCreateTags(updateTaskDto.getTags()));
        }

        // Handle due date update - allow null values
        // Note: We update the due date regardless of whether it's null or not
        // This allows clearing the due date if needed
        OffsetDateTime newDueDate = updateTaskDto.getDueDateTime();
        task.setDueDateTime(newDueDate);

        // Reset overdue notification flag if due date is moved to the future
        // This ensures the task can receive a new notification if it becomes overdue again
        if (newDueDate != null && newDueDate.isAfter(OffsetDateTime.now(ZoneOffset.UTC))) {
            task.setHasSentOverdue(false);
            log.info("Reset hasSentOverdue flag for task {} - new due date is in the future", task.getId());
        }

        // Track if any recurrence fields are being updated
        boolean recurrenceFieldsUpdated = false;

        // Update recurrence fields
        if (updateTaskDto.getIsRecurring() != null) {
            task.setIsRecurring(updateTaskDto.getIsRecurring());
            recurrenceFieldsUpdated = true;

            // If disabling recurrence, clear all recurrence-related fields
            if (!updateTaskDto.getIsRecurring()) {
                task.setRecurrenceRuleStr(null);
                task.setStartDate(null);
                task.setEndDate(null);
            }
        }
        if (updateTaskDto.getRecurrenceRuleStr() != null) {
            task.setRecurrenceRuleStr(updateTaskDto.getRecurrenceRuleStr());
            recurrenceFieldsUpdated = true;
        }
        if (updateTaskDto.getStartDate() != null) {
            task.setStartDate(updateTaskDto.getStartDate());
            recurrenceFieldsUpdated = true;
        }
        if (updateTaskDto.getEndDate() != null) {
            task.setEndDate(updateTaskDto.getEndDate());
            recurrenceFieldsUpdated = true;
        }

        // Validate recurrence configuration only if recurrence fields were updated
        if (recurrenceFieldsUpdated) {
            validateRecurrence(
                task.getIsRecurring(),
                task.getRecurrenceRuleStr(),
                task.getStartDate(),
                task.getEndDate()
            );
        }

        task.setUpdatedBy(currentUserId);
        task.setUpdatedAt(OffsetDateTime.now());
    }

    private void handleRecurringTaskEdit(Task task, UpdateTaskDto updateTaskDto, Long currentUserId) {
            // Update this instance only
            if(updateTaskDto.getRecurrenceEditMode() == RecurrenceEditMode.THIS_INSTANCE) {
                // Add exDate to current task
                String updatedRRule = appendExDate(task.getRecurrenceRuleStr(), updateTaskDto.getInstanceDate());
                task.setRecurrenceRuleStr(updatedRRule);
                taskRepository.save(task);

                // Create standalone task
                // Create new task dto
                // Prep inputs for DTO
                List<String> tagNames = task.getTags() != null
                        ? task.getTags().stream().map(Tag::getTagName).toList()
                        : null;

                // Get assigned user IDs from current task to replicate collaborators
                List<Long> assignedUserIds = collaboratorService.getCollaboratorIdsByTaskId(task.getId());

                CreateTaskDto standaloneTaskDto = new CreateTaskDto(task.getProjectId(),
                            task.getOwnerId(),
                            updateTaskDto.getTitle() != null ? updateTaskDto.getTitle() : task.getTitle(),
                            updateTaskDto.getDescription() != null ? updateTaskDto.getDescription() : task.getDescription(),
                            updateTaskDto.getStatus() != null ? updateTaskDto.getStatus() : Status.TODO,
                            updateTaskDto.getTaskType() != null ? updateTaskDto.getTaskType() : task.getTaskType(),
                            tagNames,
                            assignedUserIds,
                            updateTaskDto.getInstanceDate(),
                            false,
                            null,
                            null,
                            null);
                this.createTask(standaloneTaskDto, task.getOwnerId(), currentUserId);

            }

            // Update this and future instances
            else if(updateTaskDto.getRecurrenceEditMode() == RecurrenceEditMode.THIS_AND_FUTURE_INSTANCES) {
                // Cap original rule and create recurring task
                // Get until date
                OffsetDateTime untilDate = updateTaskDto.getInstanceDate().minusDays(1).withHour(23).withMinute(59).withSecond(59);

                String updatedRRule = appendUntilDate(task.getRecurrenceRuleStr(), untilDate);
                task.setRecurrenceRuleStr(updatedRRule);
                task.setEndDate(untilDate);
                taskRepository.save(task);

                log.info("Capped Original task {} with UNTIL: {}", task.getId(), updatedRRule);

                // Create standalone task
                // Create new task dto
                // Prep inputs for DTO
                List<String> tagNames = task.getTags() != null
                        ? task.getTags().stream().map(Tag::getTagName).toList()
                        : null;

                // Get assigned user IDs from current task to replicate collaborators
                List<Long> assignedUserIds = collaboratorService.getCollaboratorIdsByTaskId(task.getId());

                // Use original reccurring rule
                String rrule = task.getRecurrenceRuleStr();

                // Remove UNTIL=...; or UNTIL=... at the end
                rrule = rrule.replaceAll(";?UNTIL=[^;]+;?", "");

                // Clean up any accidental double semicolons
                rrule = rrule.replaceAll(";;", ";");

                // Trim just in case
                rrule = rrule.trim();

                CreateTaskDto standaloneTaskDto = new CreateTaskDto(task.getProjectId(),
                            task.getOwnerId(),
                            updateTaskDto.getTitle() != null ? updateTaskDto.getTitle() : task.getTitle(),
                            updateTaskDto.getDescription() != null ? updateTaskDto.getDescription() : task.getDescription(),
                            updateTaskDto.getStatus() != null ? updateTaskDto.getStatus() : Status.TODO,
                            updateTaskDto.getTaskType() != null ? updateTaskDto.getTaskType() : task.getTaskType(),
                            tagNames,
                            assignedUserIds,
                            updateTaskDto.getInstanceDate(),
                            false,
                            rrule,
                            null,
                            null);
                this.createTask(standaloneTaskDto, task.getOwnerId(), currentUserId);
                log.info("Create new recurring task starting from: {}", updateTaskDto.getInstanceDate());


            }

            // Update future instances only
            else if(updateTaskDto.getRecurrenceEditMode() == RecurrenceEditMode.ALL_FUTURE_INSTANCES) {
                // Change task directly
                if(updateTaskDto.getTitle() != null) {
                    task.setTitle(updateTaskDto.getTitle());
                }
                if(updateTaskDto.getDescription() != null) {
                    task.setDescription(updateTaskDto.getDescription());
                }
                if(updateTaskDto.getTaskType() != null) {
                    task.setTaskType(updateTaskDto.getTaskType());
                }
                if(updateTaskDto.getStatus() != null) {
                    task.setStatus(updateTaskDto.getStatus());
                }
                if(updateTaskDto.getTags() != null) {
                    task.getTags().clear();
                    task.getTags().addAll(tagService.findOrCreateTags(updateTaskDto.getTags()));
                }
                task.setUpdatedBy(currentUserId);
                task.setUpdatedAt(OffsetDateTime.now());
                taskRepository.save(task);
                log.info("Updated all future instances for task: {}", task.getId());

            }
    }

    private String appendExDate(String rrule, OffsetDateTime exceptionDate) {
        // Validate Input
        if(rrule == null || rrule.trim().isEmpty()) {
            throw new RuntimeException("RRULE cannot be null or empty");
        }
        if(exceptionDate == null) {
            throw new RuntimeException("Exception date cannot be null");
        }

        // Format the exception date
        String formattedDate = formatDateForRRule(exceptionDate);

        // Append EXDATE to RRULE
        return rrule + ";EXDATE=" + formattedDate;
    }

    private String appendUntilDate(String rrule, OffsetDateTime untilDate) {
        // Validate Input
        if(rrule == null || rrule.trim().isEmpty()) {
            throw new RuntimeException("RRULE cannot be null or empty");
        }
        if(untilDate == null) {
            throw new RuntimeException("Until date cannot be null");
        }

        // Format the until date
        String formattedDate = formatDateForRRule(untilDate);

        // APPEND UNTIL to RRULE
        return rrule + ";UNTIL=" + formattedDate;
    }

    private String formatDateForRRule(OffsetDateTime dateTime) {
        // Step 1: Convert to UTC timezone
        // RRule dates always in UTC
        ZonedDateTime utcDateTime = dateTime.atZoneSameInstant(ZoneOffset.UTC);

        // Format date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

        return utcDateTime.format(formatter);
    }

    // Helper method to publish status changes
    private void publishStatusChangeNotification(Task task, Status oldStatus, Status newStatus, Long editorId) {
        try {
            // Get all assignees 
            List<Long> assigneeIds = collaboratorService.getCollaboratorIdsByTaskId(task.getId());
            List<Long> recipientsToNotify = assigneeIds.stream().filter(id -> !id.equals(editorId)).toList();

            // Publish if there are recipients
            if (!recipientsToNotify.isEmpty()) {
                TaskNotificationMessageDto dto = TaskNotificationMessageDto.forStatusChange(
                    task.getId(),
                    editorId,
                    task.getProjectId(),
                    task.getTitle(),
                    oldStatus.toString(),
                    newStatus.toString(),
                    recipientsToNotify
                );

                notificationPublisher.publishTaskNotification(dto);
            }
            
        } catch(Exception e) {
            log.error("Failed to publish status change notification for task ID: {} - Error: {}",
                 task.getId(), e.getMessage(), e);
        }
    }
}
