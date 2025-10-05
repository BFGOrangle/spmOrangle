package com.spmorangle.crm.taskmanagement.service.impl;

import com.spmorangle.crm.projectmanagement.service.ProjectService;
import com.spmorangle.crm.taskmanagement.dto.*;
import com.spmorangle.crm.taskmanagement.model.Tag;
import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import com.spmorangle.crm.taskmanagement.service.CollaboratorService;
import com.spmorangle.crm.taskmanagement.service.SubtaskService;
import com.spmorangle.crm.taskmanagement.service.TagService;
import com.spmorangle.crm.taskmanagement.service.TaskService;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateTaskResponseDto createTask(CreateTaskDto createTaskDto, Long currentUserId) {
        return createTask(createTaskDto, currentUserId, currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateTaskResponseDto createTask(CreateTaskDto createTaskDto, Long taskOwnerId, Long currentUserId) {
        log.info("Creating task with title: {} for user: {}", createTaskDto.getTitle(), currentUserId);
        
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
        
        Task savedTask = taskRepository.save(task);
        log.info("Task created with ID: {}", savedTask.getId());
        
        List<Long> assignedUserIds = new ArrayList<>();
        if (createTaskDto.getAssignedUserIds() != null && !createTaskDto.getAssignedUserIds().isEmpty()) {
            for (Long userId : createTaskDto.getAssignedUserIds()) {
                try {
                    AddCollaboratorRequestDto collaboratorRequest = AddCollaboratorRequestDto.builder()
                            .taskId(savedTask.getId())
                            .collaboratorId(userId)
                            .assignedById(currentUserId)
                            .build();
                    
                    collaboratorService.addCollaborator(collaboratorRequest);
                    assignedUserIds.add(userId);
                    log.info("Task assigned to user: {}", userId);
                } catch (Exception e) {
                    log.warn("Failed to assign task to user {}: {}", userId, e.getMessage());
                }
            }
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
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getProjectTasks(Long userId, Long projectId) {
        log.info("Getting tasks for project: {}", projectId);
        List<Task> tasks = taskRepository.findByProjectIdAndNotDeleted(projectId);
        Set<Long> tasksUserIsCollaboratorFor = new HashSet<>(collaboratorService.getTasksForWhichUserIsCollaborator(userId));
        Long projectOwnerId = projectService.getOwnerId(projectId);
        return tasks.stream()
                .map((task) -> {
                    boolean userHasWriteAccess = task.getOwnerId().equals(userId) || tasksUserIsCollaboratorFor.contains(task.getId());
                    boolean userHasDeleteAccess = userId.equals(projectOwnerId);
                    return mapToTaskResponseDto(task, userHasWriteAccess, userHasDeleteAccess);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getPersonalTasks(Long userId) {
        log.info("Getting personal tasks for user: {}", userId);
        List<Task> tasks = taskRepository.findPersonalTasksByOwnerIdAndNotDeleted(userId);
        return tasks.stream()
                .map(task -> mapToTaskResponseDto(task, true, true))
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
        
        return tasks.stream()
                .map(task -> {
                    boolean userHasDeleteAccess = task.getProjectId() != null 
                        && userId.equals(projectOwnerMap.get(task.getProjectId()));
                    return mapToTaskResponseDto(task, true, userHasDeleteAccess);
                })
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
            task.setStatus(updateTaskDto.getStatus());
        }

        if (updateTaskDto.getTaskType() != null) {
            task.setTaskType(updateTaskDto.getTaskType());
        }

        if (updateTaskDto.getTags() != null) {
            task.getTags().clear();
            task.getTags().addAll(tagService.findOrCreateTags(updateTaskDto.getTags()));
        }

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



    private TaskResponseDto mapToTaskResponseDto(Task task, boolean userHasEditAccess, boolean userHasDeleteAccess) {
        // Load subtasks for this task
        List<SubtaskResponseDto> subtasks = subtaskService.getSubtasksByTaskId(task.getId());
        
        return TaskResponseDto.builder()
                .id(task.getId())
                .projectId(task.getProjectId())
                .ownerId(task.getOwnerId())
                .taskType(task.getTaskType())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .tags(task.getTags() != null 
                        ? task.getTags().stream().map(Tag::getTagName).toList() 
                        : null)
                .userHasEditAccess(userHasEditAccess)
                .userHasDeleteAccess(userHasDeleteAccess)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .createdBy(task.getCreatedBy())
                .updatedBy(task.getUpdatedBy())
                .subtasks(subtasks)
                .build();
    }
}
