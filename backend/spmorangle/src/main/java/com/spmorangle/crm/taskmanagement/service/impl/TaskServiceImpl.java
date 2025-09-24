package com.spmorangle.crm.taskmanagement.service.impl;

import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorRequestDto;
import com.spmorangle.crm.taskmanagement.dto.CreateTaskDto;
import com.spmorangle.crm.taskmanagement.dto.CreateTaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.GetTaskResponseDto;
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

    @Override
    public List<GetTaskResponseDto> getTasks(long userId){
        List<Task> userTasks = taskRepository.getTasksByOwnerId(userId);
        return userTasks.stream().map(task -> {
            List<Long> assignedUserIds = new ArrayList<>();
            
            return GetTaskResponseDto.builder()
                    .id(task.getId())
                    .projectId(task.getProjectId())
                    .ownerId(task.getOwnerId())
                    .title(task.getTitle())
                    .description(task.getDescription())
                    .status(task.getStatus())
                    .tags(task.getTags())
                    .createdBy(task.getCreatedBy())
                    .createdAt(task.getCreatedAt())
                    .assignedUserIds(assignedUserIds)
                    .build();
        }).toList();
    }

//    @Override
//    public List<GetSubtaskResponseDto> getSubtasks(long taskId){
//        /*
//        * are all subtasks viewable by owner and collaborator of main task?
//        * assume that if user can click into the task, they should be able to fetch the subtask
//        * no need for user validation
//        * */
//
//    }
}
