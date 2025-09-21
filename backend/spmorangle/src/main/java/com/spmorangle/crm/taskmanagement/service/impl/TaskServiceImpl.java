package com.spmorangle.crm.taskmanagement.service.impl;

import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorRequestDto;
import com.spmorangle.crm.taskmanagement.dto.CreateTaskDto;
import com.spmorangle.crm.taskmanagement.dto.CreateTaskResponseDto;
import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import com.spmorangle.crm.taskmanagement.service.CollaboratorService;
import com.spmorangle.crm.taskmanagement.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final CollaboratorService collaboratorService;

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
                .title(savedTask.getTitle())
                .description(savedTask.getDescription())
                .status(savedTask.getStatus())
                .assignedUserIds(assignedUserIds)
                .tags(savedTask.getTags())
                .createdBy(savedTask.getCreatedBy())
                .createdAt(savedTask.getCreatedAt())
                .build();
    }
}
