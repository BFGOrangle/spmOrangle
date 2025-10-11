package com.spmorangle.crm.taskmanagement.service.impl;

import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorRequestDto;
import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorResponseDto;
import com.spmorangle.crm.taskmanagement.dto.RemoveCollaboratorRequestDto;
import com.spmorangle.crm.taskmanagement.model.TaskAssignee;
import com.spmorangle.crm.taskmanagement.model.TaskAssigneeCK;
import com.spmorangle.crm.taskmanagement.repository.TaskAssigneeRepository;
import com.spmorangle.crm.taskmanagement.service.CollaboratorService;
import com.spmorangle.crm.taskmanagement.service.exception.CollaboratorAlreadyExistsException;
import com.spmorangle.crm.taskmanagement.service.exception.CollaboratorAssignmentNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollaboratorServiceImpl implements CollaboratorService {

    private final TaskAssigneeRepository taskAssigneeRepository;

    @Override
    public AddCollaboratorResponseDto addCollaborator(AddCollaboratorRequestDto requestDto) {
        long taskId = requestDto.getTaskId();
        long collaboratorId = requestDto.getCollaboratorId();
        long assignedById = requestDto.getAssignedById();

        log.info("🔵 [COLLABORATOR] Attempting to add collaborator - TaskId: {}, CollaboratorId: {}, AssignedById: {}", 
                 taskId, collaboratorId, assignedById);

        if (taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(taskId, collaboratorId, assignedById)) {
            log.warn("⚠️ [COLLABORATOR] Collaborator already exists - TaskId: {}, CollaboratorId: {}", taskId, collaboratorId);
            throw new CollaboratorAlreadyExistsException(taskId, collaboratorId);
        }

        TaskAssignee taskAssignee = new TaskAssignee();
        taskAssignee.setTaskId(taskId);
        taskAssignee.setUserId(collaboratorId);
        taskAssignee.setAssignedId(assignedById);

        log.info("💾 [COLLABORATOR] Saving collaborator to database...");
        TaskAssignee savedAssignee = taskAssigneeRepository.save(taskAssignee);
        log.info("✅ [COLLABORATOR] Successfully added collaborator - TaskId: {}, CollaboratorId: {}, AssignedAt: {}", 
                 savedAssignee.getTaskId(), savedAssignee.getUserId(), savedAssignee.getAssignedAt());

        return AddCollaboratorResponseDto.builder()
                .taskId(savedAssignee.getTaskId())
                .collaboratorId(savedAssignee.getUserId())
                .assignedById(savedAssignee.getAssignedId())
                .assignedAt(savedAssignee.getAssignedAt())
                .build();
    }

    @Override
    public void removeCollaborator(RemoveCollaboratorRequestDto requestDto) {
        long taskId = requestDto.getTaskId();
        long collaboratorId = requestDto.getCollaboratorId();
        long assignedById = requestDto.getAssignedById();

        if (!taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(taskId, collaboratorId, assignedById)) {
            throw new CollaboratorAssignmentNotFoundException(taskId, collaboratorId);
        }

        taskAssigneeRepository.deleteById(new TaskAssigneeCK(taskId, collaboratorId, assignedById));
    }

    @Override
    public boolean isUserTaskCollaborator(Long taskId, Long userId) {
        return taskAssigneeRepository.existsByTaskIdAndUserId(taskId, userId);
    }

    @Override
    public List<Long> getTasksForWhichUserIsCollaborator(Long userId) {
        return taskAssigneeRepository.findTaskIdsUserIsAssigneeFor(userId);
    }
}
