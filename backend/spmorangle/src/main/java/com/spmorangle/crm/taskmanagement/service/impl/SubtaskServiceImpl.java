package com.spmorangle.crm.taskmanagement.service.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.spmorangle.crm.taskmanagement.model.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spmorangle.crm.taskmanagement.dto.CreateSubtaskDto;
import com.spmorangle.crm.taskmanagement.dto.SubtaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.UpdateSubtaskDto;
import com.spmorangle.crm.taskmanagement.model.Subtask;
import com.spmorangle.crm.taskmanagement.repository.SubtaskRepository;
import com.spmorangle.crm.taskmanagement.service.SubtaskService;
import com.spmorangle.crm.projectmanagement.service.ProjectService;
import com.spmorangle.crm.taskmanagement.service.CollaboratorService;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubtaskServiceImpl implements SubtaskService {

    private final SubtaskRepository subtaskRepository;
    private final ProjectService projectService;
    private final CollaboratorService collaboratorService;
    private final TaskRepository taskRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubtaskResponseDto createSubtask(CreateSubtaskDto createSubtaskDto, Long currentUserId) {
        log.info("Creating subtask with title: {} for task: {} by user: {}", 
                createSubtaskDto.getTitle(), createSubtaskDto.getTaskId(), currentUserId);
        
        Subtask subtask = new Subtask();
        subtask.setTaskId(createSubtaskDto.getTaskId());
        subtask.setProjectId(createSubtaskDto.getProjectId());
        subtask.setTaskType(createSubtaskDto.getTaskType());
        subtask.setTitle(createSubtaskDto.getTitle());
        subtask.setDetails(createSubtaskDto.getDetails());
        subtask.setStatus(createSubtaskDto.getStatus());
        subtask.setCreatedBy(currentUserId);
        subtask.setCreatedAt(OffsetDateTime.now());
        
        Subtask savedSubtask = subtaskRepository.save(subtask);
        log.info("Subtask created with ID: {}", savedSubtask.getId());
        
        return mapToSubtaskResponseDto(savedSubtask, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubtaskResponseDto> getSubtasksByTaskId(Long taskId, Long currentUserId) {
        log.info("Fetching subtasks for task: {}", taskId);
        
        List<Subtask> subtasks = subtaskRepository.findByTaskIdAndNotDeleted(taskId);
        
        return subtasks.stream()
                .map(subtask -> mapToSubtaskResponseDto(subtask, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubtaskResponseDto> getSubtasksByProjectId(Long projectId, Long currentUserId) {
        log.info("Fetching subtasks for project: {}", projectId);
        
        List<Subtask> subtasks = subtaskRepository.findByProjectIdAndNotDeleted(projectId);
        
        return subtasks.stream()
                .map(subtask -> mapToSubtaskResponseDto(subtask, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubtaskResponseDto updateSubtask(Long subtaskId, UpdateSubtaskDto updateSubtaskDto, Long currentUserId) {
        log.info("Updating subtask: {} by user: {}", subtaskId, currentUserId);

        if (!canUserUpdateSubtask(subtaskId, currentUserId)) {
            throw new RuntimeException("Only project owner or collaborators can update the subtask");
        }
        
        Subtask subtask = subtaskRepository.findByIdAndNotDeleted(subtaskId);
        if (subtask == null) {
            throw new RuntimeException("Subtask not found with ID: " + subtaskId);
        }
        
        // Update only non-null fields
        if (updateSubtaskDto.getTitle() != null) {
            subtask.setTitle(updateSubtaskDto.getTitle());
        }
        if (updateSubtaskDto.getDetails() != null) {
            subtask.setDetails(updateSubtaskDto.getDetails());
        }
        if (updateSubtaskDto.getStatus() != null) {
            subtask.setStatus(updateSubtaskDto.getStatus());
        }
        if (updateSubtaskDto.getTaskType() != null) {
            subtask.setTaskType(updateSubtaskDto.getTaskType());
        }
        
        subtask.setUpdatedBy(currentUserId);
        subtask.setUpdatedAt(OffsetDateTime.now());
        
        Subtask updatedSubtask = subtaskRepository.save(subtask);
        log.info("Subtask {} updated successfully", subtaskId);
        
        return mapToSubtaskResponseDto(updatedSubtask, currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSubtask(Long subtaskId, Long currentUserId) {
        log.info("Deleting subtask: {} by user: {}", subtaskId, currentUserId);

        if (!canUserDeleteSubtask(subtaskId, currentUserId)) {
            throw new RuntimeException("Only project owner can delete the subtask");
        }

        Subtask subtask = subtaskRepository.findByIdAndNotDeleted(subtaskId);
        if (subtask == null) {
            throw new RuntimeException("Subtask not found with ID: " + subtaskId);
        }
        
        subtask.setDeleteInd(true);
        subtask.setUpdatedBy(currentUserId);
        subtask.setUpdatedAt(OffsetDateTime.now());
        
        subtaskRepository.save(subtask);
        log.info("Subtask {} marked as deleted", subtaskId);
    }

    @Override
    @Transactional(readOnly = true)
    public SubtaskResponseDto getSubtaskById(Long subtaskId, Long currentUserId) {
        log.info("Fetching subtask: {}", subtaskId);
        
        Subtask subtask = subtaskRepository.findByIdAndNotDeleted(subtaskId);
        if (subtask == null) {
            throw new RuntimeException("Subtask not found with ID: " + subtaskId);
        }
        
        return mapToSubtaskResponseDto(subtask, currentUserId);
    }

    private SubtaskResponseDto mapToSubtaskResponseDto(Subtask subtask, Long currentUserId) {
        boolean userHasEditAccess = canUserUpdateSubtask(subtask.getId(), currentUserId);
        boolean userHasDeleteAccess = canUserDeleteSubtask(subtask.getId(), currentUserId);
        
        return SubtaskResponseDto.builder()
                .id(subtask.getId())
                .taskId(subtask.getTaskId())
                .projectId(subtask.getProjectId())
                .taskType(subtask.getTaskType())
                .title(subtask.getTitle())
                .details(subtask.getDetails())
                .status(subtask.getStatus())
                .userHasEditAccess(userHasEditAccess)
                .userHasDeleteAccess(userHasDeleteAccess)
                .createdAt(subtask.getCreatedAt())
                .updatedAt(subtask.getUpdatedAt())
                .createdBy(subtask.getCreatedBy())
                .updatedBy(subtask.getUpdatedBy())
                .build();
    }

    @Override
    public boolean canUserUpdateSubtask(Long subtaskId, Long userId) {
        Subtask subtask = subtaskRepository.findByIdAndNotDeleted(subtaskId);
        if (subtask == null) {
            throw new RuntimeException("Subtask not found with ID: " + subtaskId);
        }

        Long projectId = subtask.getProjectId();
        Long taskId = subtask.getTaskId();

        if (projectId == null) {
            return subtask.getCreatedBy().equals(userId);
        }

        Task task = taskRepository.findByIdAndNotDeleted(taskId);
        if (task == null) {
            throw new RuntimeException("Task not found with ID: " + taskId);
        }

        // check if is a collaborator of the task
        return task.getOwnerId().equals(userId) || collaboratorService.isUserTaskCollaborator(taskId, userId);

    }

    @Override
    public boolean canUserDeleteSubtask(Long subtaskId, Long userId) {
        Subtask subtask = subtaskRepository.findByIdAndNotDeleted(subtaskId);
        if (subtask == null) {
            throw new RuntimeException("Subtask not found with ID: " + subtaskId);
        }

        Long projectId = subtask.getProjectId();

        if (projectId == null) {
            return subtask.getCreatedBy().equals(userId);
        }

        return projectService.getOwnerId(projectId).equals(userId);
    }
}
