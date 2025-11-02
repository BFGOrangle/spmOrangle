package com.spmorangle.crm.taskmanagement.service.impl;

import com.spmorangle.common.model.User;
import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.crm.projectmanagement.dto.ProjectResponseDto;
import com.spmorangle.crm.projectmanagement.service.ProjectService;
import com.spmorangle.crm.taskmanagement.dto.*;
import com.spmorangle.crm.taskmanagement.enums.CalendarView;
import com.spmorangle.crm.taskmanagement.enums.RecurrenceEditMode;
import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.enums.TaskType;
import com.spmorangle.crm.taskmanagement.model.Tag;
import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.repository.TaskAssigneeRepository;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import com.spmorangle.crm.taskmanagement.service.*;
import com.spmorangle.crm.notification.messaging.publisher.NotificationMessagePublisher;
import com.spmorangle.crm.departmentmgmt.service.DepartmentQueryService;
import com.spmorangle.crm.departmentmgmt.service.DepartmentalVisibilityService;
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
    private final DepartmentQueryService departmentQueryService;
    private final DepartmentalVisibilityService departmentalVisibilityService;

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

        // Set priority, defaulting to 5 (medium) if not provided
        task.setPriority(createTaskDto.getPriority() != null ? createTaskDto.getPriority() : 5);

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
                .priority(savedTask.getPriority())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getProjectTasks(Long userId, Long projectId) {
        log.info("Getting tasks for project: {}", projectId);
        List<Task> tasks = taskRepository.findByProjectIdAndNotDeleted(projectId);

        Set<Long> visibleDepartmentIds = getUserVisibleDepartmentIds(userId);
        tasks = tasks.stream()
                .filter(task -> canUserSeeTaskByDepartment(task, visibleDepartmentIds))
                .collect(Collectors.toList());

        Set<Long> tasksUserIsCollaboratorFor = new HashSet<>(collaboratorService.getTasksForWhichUserIsCollaborator(userId));
        Long projectOwnerId = projectService.getOwnerId(projectId);

        Map<Long, String> projectNames = resolveProjectNames(Collections.singleton(projectId));
        Map<Long, User> ownerDetails = resolveOwnerDetails(tasks);

        List<TaskResponseDto> result = new ArrayList<>();

        for(Task task : tasks) {
            boolean userHasWriteAccess = task.getOwnerId().equals(userId) || tasksUserIsCollaboratorFor.contains(task.getId());
            boolean userHasDeleteAccess = userId.equals(projectOwnerId);

            result.add(mapToTaskResponseDto(task, userHasWriteAccess, userHasDeleteAccess, projectNames, ownerDetails, userId));
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getProjectTasksForCalendar(Long userId, Long projectId, CalendarView calendarView, OffsetDateTime referenceDate) {
        log.info("Getting tasks for project on calendar view: {}", projectId);
        List<Task> tasks = taskRepository.findByProjectIdAndNotDeleted(projectId);

        Set<Long> visibleDepartmentIds = getUserVisibleDepartmentIds(userId);
        tasks = tasks.stream()
                .filter(task -> canUserSeeTaskByDepartment(task, visibleDepartmentIds))
                .collect(Collectors.toList());

        Set<Long> tasksUserIsCollaboratorFor = new HashSet<>(collaboratorService.getTasksForWhichUserIsCollaborator(userId));
        Long projectOwnerId = projectService.getOwnerId(projectId);

        Map<Long, String> projectNames = resolveProjectNames(Collections.singleton(projectId));
        Map<Long, User> ownerDetails = resolveOwnerDetails(tasks);

        List<TaskResponseDto> result = new ArrayList<>();

        for(Task task : tasks) {
            boolean userHasWriteAccess = task.getOwnerId().equals(userId) || tasksUserIsCollaboratorFor.contains(task.getId());
            boolean userHasDeleteAccess = userId.equals(projectOwnerId);

            // Return recurring virtual tasks (but not if completed - avoid duplicates)
            if(Boolean.TRUE.equals(task.getIsRecurring()) && task.getStatus() != Status.COMPLETED) {
                List<TaskResponseDto> virtualInstances = expandRecurringTaskForDisplay(
                    task,
                    userHasWriteAccess,
                    userHasDeleteAccess,
                    projectNames,
                    ownerDetails,
                    userId,
                    calendarView,
                    referenceDate
                );
                result.addAll(virtualInstances);
            } else {
                result.add(mapToTaskResponseDto(task, userHasWriteAccess, userHasDeleteAccess, projectNames, ownerDetails, userId));
            }
        }

        return result;
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
    public List<TaskResponseDto> getPersonalTasksForCalendar(Long userId, CalendarView calendarView, OffsetDateTime referenceDate) {
        log.info("Getting personal tasks for user: {}", userId);
        List<Task> tasks = taskRepository.findPersonalTasksByOwnerIdAndNotDeleted(userId);
        Map<Long, User> ownerDetails = resolveOwnerDetails(tasks);

        List<TaskResponseDto> result = new ArrayList<>();

        for(Task task : tasks) {
            // Return recurring virtual tasks
            if(Boolean.TRUE.equals(task.getIsRecurring())) {
                List<TaskResponseDto> virtualInstances = expandRecurringTaskForDisplay(
                    task,
                    true,
                    true,
                    Collections.emptyMap(),
                    ownerDetails,
                    userId,
                    calendarView,
                    referenceDate
                );
                result.addAll(virtualInstances);
            } else {
                result.add(mapToTaskResponseDto(task, true, true, Collections.emptyMap(), ownerDetails, userId));
            }
        }

        return result;

    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getAllUserTasks(Long userId) {
        log.info("Getting all tasks for user: {}", userId);
        List<Task> tasks = taskRepository.findUserTasks(userId);

        Set<Long> visibleDepartmentIds = getUserVisibleDepartmentIds(userId);
        tasks = tasks.stream()
                .filter(task -> canUserSeeTaskByDepartment(task, visibleDepartmentIds))
                .collect(Collectors.toList());

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

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getAllUserTasksForCalendar(Long userId, CalendarView calendarView, OffsetDateTime referenceDate) {
        log.info("Getting all tasks for user: {}", userId);
        List<Task> tasks = taskRepository.findUserTasks(userId);

        Set<Long> visibleDepartmentIds = getUserVisibleDepartmentIds(userId);
        tasks = tasks.stream()
                .filter(task -> canUserSeeTaskByDepartment(task, visibleDepartmentIds))
                .collect(Collectors.toList());

        Set<Long> projectIds = tasks.stream()
                .map(Task::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Long> projectOwnerMap = projectService.getProjectOwners(projectIds);
        Map<Long, String> projectNames = resolveProjectNames(projectIds);
        Map<Long, User> ownerDetails = resolveOwnerDetails(tasks);

        Set<Long> tasksUserIsCollaboratorFor = new HashSet<>(collaboratorService.getTasksForWhichUserIsCollaborator(userId));

        List<TaskResponseDto> result = new ArrayList<>();

        for(Task task : tasks) {

            // Long projectOwnerId = projectService.getOwnerId(task.getProjectId());

            boolean userHasWriteAccess = task.getOwnerId().equals(userId) || tasksUserIsCollaboratorFor.contains(task.getId());
            boolean userHasDeleteAccess = task.getProjectId() != null ? userId.equals(projectOwnerMap.get(task.getProjectId())) : task.getOwnerId().equals(userId);

            // Return recurring virtual tasks (but not if completed - avoid duplicates)
            if(Boolean.TRUE.equals(task.getIsRecurring()) && task.getStatus() != Status.COMPLETED) {
                List<TaskResponseDto> virtualInstances = expandRecurringTaskForDisplay(
                    task,
                    userHasWriteAccess,
                    userHasDeleteAccess,
                    projectNames,
                    ownerDetails,
                    userId,
                    calendarView,
                    referenceDate
                );
                result.addAll(virtualInstances);
            } else {
                result.add(mapToTaskResponseDto(task, userHasWriteAccess, userHasDeleteAccess, projectNames, ownerDetails, userId));
            }
        }

        return result;
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

        Set <Long> visibleDepartmentIds = getUserVisibleDepartmentIds(userId);

        List<User> departmentMembers = userRepository.findByDepartmentIds(visibleDepartmentIds, userId);

        // List<User> departmentMembers = userRepository.findByDepartmentIgnoreCase(department).stream()
        //         .filter(user -> !Objects.equals(user.getId(), userId))
        //         .toList();

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

        // Capture old status before any updates (needed for logic below)
        Status oldStatus = task.getStatus();

        // Handle time tracking for status transitions (before any updates)
        if (updateTaskDto.getStatus() != null) {
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

        // Handle reverting a recurring task from COMPLETED back to TODO/IN_PROGRESS
        else if (oldStatus == Status.COMPLETED &&
                 (updateTaskDto.getStatus() == Status.TODO || updateTaskDto.getStatus() == Status.IN_PROGRESS) &&
                 Boolean.TRUE.equals(task.getIsRecurring())) {
            log.info("Recurring task moved from COMPLETED back to {}, checking for duplicate next instance", updateTaskDto.getStatus());

            // Apply field updates first
            applyFieldUpdates(task, updateTaskDto, currentUserId);

            // Try to find and delete the "next instance" that was created
            // Look for a task with same title, recurrence rule, created very recently (last 5 minutes)
            try {
                OffsetDateTime fiveMinutesAgo = OffsetDateTime.now().minusMinutes(5);
                List<Task> potentialDuplicates = taskRepository.findAll().stream()
                    .filter(t -> !Objects.equals(t.getId(), task.getId())) // Not the same task
                    .filter(t -> Boolean.TRUE.equals(t.getIsRecurring())) // Is recurring
                    .filter(t -> t.getTitle().equals(task.getTitle())) // Same title
                    .filter(t -> t.getRecurrenceRuleStr() != null &&
                                 t.getRecurrenceRuleStr().equals(task.getRecurrenceRuleStr())) // Same recurrence rule
                    .filter(t -> t.getStatus() == Status.TODO) // Status is TODO
                    .filter(t -> t.getCreatedAt().isAfter(fiveMinutesAgo)) // Created recently
                    .filter(t -> t.getProjectId() != null ?
                                 t.getProjectId().equals(task.getProjectId()) :
                                 task.getProjectId() == null) // Same project (or both null)
                    .toList();

                if (!potentialDuplicates.isEmpty()) {
                    // Delete the most recently created duplicate (likely the auto-generated next instance)
                    Task duplicate = potentialDuplicates.stream()
                        .max((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()))
                        .orElse(null);

                    if (duplicate != null) {
                        log.info("Found and deleting duplicate next instance: {}", duplicate.getId());
                        taskRepository.delete(duplicate);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to clean up duplicate next instance: {}", e.getMessage());
                // Don't fail the update if cleanup fails
            }
        }

        // Generate next instance of task if marked completed
        else if (updateTaskDto.getStatus() == Status.COMPLETED && Boolean.TRUE.equals(task.getIsRecurring())) {
            log.info("Task marked completed, trying to generate new instance");
            // Apply all field updates to current task before creating next instance
            applyFieldUpdates(task, updateTaskDto, currentUserId);
            log.info("After apply field updates");

            // Check if task has recurrence rule (only required field)
            if (task.getRecurrenceRuleStr() != null) {
                log.info("Task has recurrence rule, calculating next instance");
                try {
                    // Determine reference date for calculating next occurrence
                    // Priority: dueDateTime > startDate > now (immediate start)
                    OffsetDateTime referenceDate;
                    if (task.getDueDateTime() != null) {
                        referenceDate = task.getDueDateTime();
                        log.info("Using dueDateTime as reference: {}", referenceDate);
                    } else if (task.getStartDate() != null) {
                        referenceDate = task.getStartDate();
                        log.info("Using startDate as reference: {}", referenceDate);
                    } else {
                        referenceDate = OffsetDateTime.now();
                        log.info("No dates set, starting immediately from: {}", referenceDate);
                    }

                    // Calculate next occurrence from day after reference
                    OffsetDateTime nextStart = referenceDate.plusDays(1);

                    // Determine effective end date for recurrence generation
                    // If endDate is null, use 1 year ahead (only affects this single next instance lookup)
                    OffsetDateTime effectiveEndDate = task.getEndDate() != null
                        ? task.getEndDate()
                        : OffsetDateTime.now().plusYears(1);

                    log.info("Generating next occurrence from {} to {}", nextStart, effectiveEndDate);

                    List<OffsetDateTime> nextOccurrences = recurrenceService.generateOccurrence(
                        task.getRecurrenceRuleStr(),
                        nextStart,
                        effectiveEndDate
                    );

                    // Only create next task if there are future occurrences
                    if (!nextOccurrences.isEmpty()) {
                        // Get the first future occurrence
                        OffsetDateTime nextOccurrence = nextOccurrences.get(0);
                        log.info("Next occurrence calculated: {}", nextOccurrence);

                        // Prep inputs for DTO
                        List<String> tagNames = task.getTags() != null
                                ? task.getTags().stream().map(Tag::getTagName).toList()
                                : null;

                        // Get assigned user IDs from current task to replicate collaborators
                        List<Long> assignedUserIds = collaboratorService.getCollaboratorIdsByTaskId(task.getId());

                        // IMPORTANT: Preserve the dueDateTime behavior
                        // - If original task had dueDateTime, set next task's dueDateTime to nextOccurrence
                        // - If original task had NO dueDateTime (null), keep next task's dueDateTime as null
                        OffsetDateTime nextDueDateTime = task.getDueDateTime() != null
                            ? nextOccurrence  // Set to calculated occurrence
                            : null;            // Keep as null

                        log.info("Next task dueDateTime: {}", nextDueDateTime);

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
                            nextDueDateTime,     // null if original had null, otherwise nextOccurrence
                            task.getIsRecurring(),
                            task.getRecurrenceRuleStr(),
                            nextOccurrence,      // startDate always set to the occurrence
                            task.getEndDate(),   // Preserve original endDate (can be null)
                            task.getPriority()   // Preserve priority from completed task
                        );

                        this.createTask(recurringTaskDto, task.getOwnerId(), currentUserId);
                        log.info("✅ Created recurring task instance with startDate: {} and dueDateTime: {} for task: {}",
                            nextOccurrence, nextDueDateTime, task.getId());
                    } else {
                        log.info("No more occurrences for recurring task: {}", task.getId());
                    }
                } catch (Exception e) {
                    log.error("❌ Failed to create next recurring task instance for task {}: {}", task.getId(), e.getMessage(), e);
                    // Don't fail the update if recurring task creation fails
                }
            } else {
                log.warn("Cannot create next recurring instance for task {}: missing recurrenceRuleStr", task.getId());
            }
        }

        else {
            // Regular task update - apply all field updates
            log.info("Priority {} - applying regular field updates", updateTaskDto.getPriority());
            applyFieldUpdates(task, updateTaskDto, currentUserId);
        }

        Task updatedTask = taskRepository.save(task);
        log.info("Task {} updated successfully, priority {}", updatedTask.getId(), updatedTask.getPriority());

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
                .priority(updatedTask.getPriority())
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

        Set<Long> visibleDepartmentIds = getUserVisibleDepartmentIds(userId);
        tasks = tasks.stream()
            .filter(task -> canUserSeeTaskByDepartment(task, visibleDepartmentIds))
            .collect(Collectors.toList());

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

        Set<Long> visibleDepartmentIds = getUserVisibleDepartmentIds(userId);
        tasks = tasks.stream()
            .filter(task -> canUserSeeTaskByDepartment(task, visibleDepartmentIds))
            .collect(Collectors.toList());
            
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
                .priority(task.getPriority())
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
                .priority(task.getPriority())
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

        if (updateTaskDto.getPriority() != null) {
            task.setPriority(updateTaskDto.getPriority());
        }

        if (updateTaskDto.getTags() != null) {
            task.getTags().clear();
            task.getTags().addAll(tagService.findOrCreateTags(updateTaskDto.getTags()));
        }

        // Handle due date update - allow null values
        // Note: We update the due date regardless of whether it's null or not
        // This allows clearing the due date if needed
        OffsetDateTime newDueDate = updateTaskDto.getDueDateTime();

        // Capture previous due date so we can detect changes
        OffsetDateTime oldDueDate = task.getDueDateTime();

        // Apply the new due date
        task.setDueDateTime(newDueDate);

        // If the due date actually changed, mark as rescheduled and reset notification flags so
        // the scheduler will cancel previous reminders and schedule a new 12-hour-before reminder.
        if (!Objects.equals(oldDueDate, newDueDate)) {
            if (newDueDate != null) {
                task.setIsRescheduled(true);
                // Reset pre-due and overdue flags so new notifications can be sent
                task.setHasSentPreDue(false);
                task.setHasSentOverdue(false);
                log.info("Due date changed for task {} - marked as rescheduled and reset notifications (old={}, new={})", task.getId(), oldDueDate, newDueDate);
            } else {
                // If due date was cleared, clear rescheduled flag and reset notifications
                task.setIsRescheduled(false);
                task.setHasSentPreDue(false);
                task.setHasSentOverdue(false);
                log.info("Due date cleared for task {} - cleared rescheduled flag and reset notifications (old={})", task.getId(), oldDueDate);
            }
        }

        if (newDueDate != null) {
            task.setIsRescheduled(true);
            task.setHasSentPreDue(false);
            log.info("Reset hasSentPreDue flag & set isRescheduled flag for task {} to true", task.getId());
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
                            null,
                            updateTaskDto.getPriority() != null ? updateTaskDto.getPriority() : task.getPriority());
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
                            null,
                            updateTaskDto.getPriority() != null ? updateTaskDto.getPriority() : task.getPriority());
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

    // Expand recurring task template into virtual instances
    private List<TaskResponseDto> expandRecurringTaskForDisplay(
        Task template,
        boolean userHasWriteAccess,
        boolean userHasDeleteAccess,
        Map<Long, String> projectNames,
        Map<Long, User> ownerDetails,
        Long userId,
        CalendarView calendarView,
        OffsetDateTime referenceDate
    ) {
        List<TaskResponseDto> virtualInstances = new ArrayList<>();

        // Get proper calendar view window using the provided reference date (or now if null)
        OffsetDateTime refDate = referenceDate != null ? referenceDate : OffsetDateTime.now();
        CalendarView.CalendarViewWindow viewWindow = calendarView.getCalendarViewWindow(refDate);
        OffsetDateTime windowStart = viewWindow.getStart();
        OffsetDateTime windowEnd = viewWindow.getEnd();

        OffsetDateTime effectiveEnd = template.getEndDate();
        if(effectiveEnd == null || effectiveEnd.isAfter(windowEnd)) {
            effectiveEnd = windowEnd;
        }

        // Determine effective start date for generating occurrences
        // Priority: template.startDate > template.dueDateTime > now()
        OffsetDateTime effectiveStart;
        if(template.getStartDate() != null) {
            effectiveStart = template.getStartDate();
        } else if(template.getDueDateTime() != null) {
            effectiveStart = template.getDueDateTime();
        } else {
            effectiveStart = OffsetDateTime.now();
        }

        // Adjust to calendar window if needed
        if(effectiveStart.isBefore(windowStart)) {
            effectiveStart = windowStart;
        }

        // Skip task if range doesnt overlap with view window
        if(effectiveStart.isAfter(effectiveEnd)) {
            log.info("Task {} range doesn't overlap with calendar view window", template.getId());
            return virtualInstances;
        }

        // Get dates that are appropriate
        try {
            log.info("Generating occurrences for task {} from {} to {}", template.getId(), effectiveStart, effectiveEnd);
            List<OffsetDateTime> occurrences = recurrenceService.generateOccurrence(template.getRecurrenceRuleStr(), effectiveStart, effectiveEnd);
            log.info("Generated {} occurrences for task {}", occurrences.size(), template.getId());

            for(OffsetDateTime occurrence : occurrences) {
                TaskResponseDto virtualDto = createVirtualInstanceDto(
                template,
                occurrence,
                userHasWriteAccess,
                userHasDeleteAccess,
                projectNames,
                ownerDetails,
                userId
                );
                log.info("Created virtual instance: dueDateTime={}, startDate={}", virtualDto.getDueDateTime(), virtualDto.getStartDate());
                virtualInstances.add(virtualDto);
            }

            log.info("Expanded recurring task {} into {} virtual instances", template.getId(), virtualInstances.size());
        } catch (Exception e) {
            log.error("Failed to expand recurring task {}: {}", template.getId(), e.getMessage(), e);
        }

        return virtualInstances;


    }

    private TaskResponseDto createVirtualInstanceDto(
        Task template,
        OffsetDateTime occurrence,
        boolean userHasWriteAccess,
        boolean userHasDeleteAccess,
        Map<Long, String> projectNames,
        Map<Long, User> ownerDetails,
        Long userId
    ) {
        List<SubtaskResponseDto> subtasks = subtaskService.getSubtasksByTaskId(template.getId(), userId);
        List<Long> assignedUserIds = collaboratorService.getCollaboratorIdsByTaskId(template.getId());

        User owner = ownerDetails != null ? ownerDetails.get(template.getOwnerId()) : null;
        String projectName = null;
        if(projectNames != null && template.getProjectId() != null) {
            projectName = projectNames.get(template.getProjectId());
        }

        // Build DTO for virtual instance
        // - dueDateTime: Only set if template has a due date (null otherwise)
        // - startDate: Always set to occurrence (determines which calendar day to show task)
        OffsetDateTime virtualDueDateTime = template.getDueDateTime() != null ? occurrence : null;

        return TaskResponseDto.builder()
            .id(template.getId())
            .projectId(template.getProjectId())
            .projectName(projectName)
            .ownerId(template.getOwnerId())
            .ownerName(owner != null ? owner.getUserName() : null)
            .ownerDepartment(owner != null ? owner.getDepartment() : null)
            .taskType(template.getTaskType())
            .title(template.getTitle())
            .description(template.getDescription())
            .status(template.getStatus())
            .tags(template.getTags() != null
                    ? template.getTags().stream().map(Tag::getTagName).toList()
                    : null)
            .assignedUserIds(assignedUserIds)
            .userHasEditAccess(userHasWriteAccess)
            .userHasDeleteAccess(userHasDeleteAccess)
            .createdAt(template.getCreatedAt())
            .updatedAt(template.getUpdatedAt())
            .createdBy(template.getCreatedBy())
            .updatedBy(template.getUpdatedBy())
            .dueDateTime(virtualDueDateTime)  // Null if template has no due date
            .subtasks(subtasks)
            .isRecurring(true)
            .recurrenceRuleStr(template.getRecurrenceRuleStr())
            .startDate(occurrence)  // Always set to occurrence for calendar day filtering
            .endDate(template.getEndDate())
            .priority(template.getPriority())
            .build();
    }

    private Set<Long> getUserVisibleDepartmentIds(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // Get department of user
        String userDepartmentName = user.getDepartment();
        if (userDepartmentName == null || userDepartmentName.isBlank()) {
            log.warn("User {} has no department assigned", userId);
            return Collections.emptySet();
        }

        // Check if what other departments are visible to our user
        return departmentQueryService.getByNameCaseInsensitive(userDepartmentName)
            .map(deptDto -> departmentalVisibilityService.visibilityDepartmentsForAssignedDept(deptDto.getId()))
            .orElse(Collections.emptySet());
    }

    private Boolean canUserSeeTaskByDepartment(Task task, Set<Long> userVisibleDepartmentIds) {
        if (userVisibleDepartmentIds.isEmpty()) {
            return true;
        }

        List<Long> assigneeIds = taskAssigneeRepository.findAssigneeIdsByTaskId(task.getId());
        assigneeIds.add(task.getOwnerId());

        for(Long assigneeId : assigneeIds) {
            User assignee = userRepository.findById(assigneeId).orElse(null);
            if(assignee == null || assignee.getDepartment() == null) {
                continue;
            }

            Long assigneeDeptId = departmentQueryService.getByNameCaseInsensitive(assignee.getDepartment())
                .map(deptDto -> deptDto.getId())
                .orElse(null);

            if(assigneeDeptId == null) {
                continue;
            }

            if(departmentalVisibilityService.canUserSeeTask(userVisibleDepartmentIds, assigneeDeptId)) {
                return true;
            }
        }

        return false;
    }
}
