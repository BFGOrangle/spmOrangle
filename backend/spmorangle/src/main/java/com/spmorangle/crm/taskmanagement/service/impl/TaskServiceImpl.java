package com.spmorangle.crm.taskmanagement.service.impl;

import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorRequestDto;
import com.spmorangle.crm.taskmanagement.dto.CreateTaskDto;
import com.spmorangle.crm.taskmanagement.dto.CreateTaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.SubtaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.TaskResponseDto;
import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import com.spmorangle.crm.taskmanagement.service.CollaboratorService;
import com.spmorangle.crm.taskmanagement.service.SubtaskService;
import com.spmorangle.crm.taskmanagement.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final CollaboratorService collaboratorService;
    private final SubtaskService subtaskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateTaskResponseDto createTask(CreateTaskDto createTaskDto, Long currentUserId) {
        log.info("Creating task with title: {} for user: {}", createTaskDto.getTitle(), currentUserId);
        
        Task task = new Task();
        
        task.setProjectId(createTaskDto.getProjectId());
        task.setOwnerId(currentUserId);
        task.setTaskType(createTaskDto.getTaskType());
        task.setTitle(createTaskDto.getTitle());
        task.setDescription(createTaskDto.getDescription());
        task.setStatus(createTaskDto.getStatus());
        task.setTags(createTaskDto.getTags());
        task.setCreatedBy(currentUserId);
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
                .tags(savedTask.getTags())
                .createdBy(savedTask.getCreatedBy())
                .createdAt(savedTask.getCreatedAt())
                .build();
    }

    @Override
    public List<TaskResponseDto> getProjectTasks(Long userId, Long projectId) {
        log.info("Getting tasks for project: {}", projectId);
        List<Task> tasks = taskRepository.findByProjectIdAndNotDeleted(projectId);
        Set<Long> tasksUserIsCollaboratorFor = new HashSet<>(collaboratorService.getTasksForWhichUserIsCollaborator(userId));
        return tasks.stream()
                .map((task) -> {
                    boolean userHasWriteAccess = task.getOwnerId().equals(userId) || tasksUserIsCollaboratorFor.contains(task.getId());
                    return mapToTaskResponseDto(task, userHasWriteAccess);
                })
                .toList();
    }

    @Override
    public List<TaskResponseDto> getPersonalTasks(Long userId) {
        log.info("Getting personal tasks for user: {}", userId);
        List<Task> tasks = taskRepository.findPersonalTasksByOwnerIdAndNotDeleted(userId);
        return tasks.stream()
                .map(task -> mapToTaskResponseDto(task, true))
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponseDto> getAllUserTasks(Long userId) {
        log.info("Getting all tasks for user: {}", userId);
        List<Task> tasks = taskRepository.findUserTasks(userId);
        return tasks.stream()
                .map(task -> mapToTaskResponseDto(task, true))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long taskId, Long currentUserId) {
        log.info("Deleting task: {} by user: {}", taskId, currentUserId);
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        // Only owner can delete the task
        if (!task.getOwnerId().equals(currentUserId)) {
            throw new RuntimeException("Only task owner can delete the task");
        }
        
        task.setDeleteInd(true);
        task.setUpdatedBy(currentUserId);
        task.setUpdatedAt(OffsetDateTime.now());
        
        taskRepository.save(task);
        log.info("Task {} marked as deleted", taskId);
    }

    private TaskResponseDto mapToTaskResponseDto(Task task, boolean userHasEditAccess) {
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
                .tags(task.getTags())
                .userHasEditAccess(userHasEditAccess)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .createdBy(task.getCreatedBy())
                .updatedBy(task.getUpdatedBy())
                .subtasks(subtasks)
                .build();
    }
}
