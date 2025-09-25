package com.spmorangle.crm.taskmanagement.service.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spmorangle.crm.taskmanagement.dto.CreateSubtaskDto;
import com.spmorangle.crm.taskmanagement.dto.SubtaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.UpdateSubtaskDto;
import com.spmorangle.crm.taskmanagement.model.Subtask;
import com.spmorangle.crm.taskmanagement.repository.SubtaskRepository;
import com.spmorangle.crm.taskmanagement.service.SubtaskService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubtaskServiceImpl implements SubtaskService {

    private final SubtaskRepository subtaskRepository;

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
        
        return mapToSubtaskResponseDto(savedSubtask);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubtaskResponseDto> getSubtasksByTaskId(Long taskId) {
        log.info("Fetching subtasks for task: {}", taskId);
        
        List<Subtask> subtasks = subtaskRepository.findByTaskIdAndNotDeleted(taskId);
        
        return subtasks.stream()
                .map(this::mapToSubtaskResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubtaskResponseDto> getSubtasksByProjectId(Long projectId) {
        log.info("Fetching subtasks for project: {}", projectId);
        
        List<Subtask> subtasks = subtaskRepository.findByProjectIdAndNotDeleted(projectId);
        
        return subtasks.stream()
                .map(this::mapToSubtaskResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubtaskResponseDto updateSubtask(Long subtaskId, UpdateSubtaskDto updateSubtaskDto, Long currentUserId) {
        log.info("Updating subtask: {} by user: {}", subtaskId, currentUserId);
        
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
        
        return mapToSubtaskResponseDto(updatedSubtask);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSubtask(Long subtaskId, Long currentUserId) {
        log.info("Deleting subtask: {} by user: {}", subtaskId, currentUserId);
        
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
    public SubtaskResponseDto getSubtaskById(Long subtaskId) {
        log.info("Fetching subtask: {}", subtaskId);
        
        Subtask subtask = subtaskRepository.findByIdAndNotDeleted(subtaskId);
        if (subtask == null) {
            throw new RuntimeException("Subtask not found with ID: " + subtaskId);
        }
        
        return mapToSubtaskResponseDto(subtask);
    }

    private SubtaskResponseDto mapToSubtaskResponseDto(Subtask subtask) {
        return SubtaskResponseDto.builder()
                .id(subtask.getId())
                .taskId(subtask.getTaskId())
                .projectId(subtask.getProjectId())
                .taskType(subtask.getTaskType())
                .title(subtask.getTitle())
                .details(subtask.getDetails())
                .status(subtask.getStatus())
                .createdAt(subtask.getCreatedAt())
                .updatedAt(subtask.getUpdatedAt())
                .createdBy(subtask.getCreatedBy())
                .updatedBy(subtask.getUpdatedBy())
                .build();
    }
}
