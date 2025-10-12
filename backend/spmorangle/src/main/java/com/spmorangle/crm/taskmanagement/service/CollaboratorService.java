package com.spmorangle.crm.taskmanagement.service;

import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorRequestDto;
import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorResponseDto;
import com.spmorangle.crm.taskmanagement.dto.RemoveCollaboratorRequestDto;

import java.util.List;

public interface CollaboratorService {

    AddCollaboratorResponseDto addCollaborator(AddCollaboratorRequestDto requestDto, Long assignedById);

    void removeCollaborator(RemoveCollaboratorRequestDto requestDto, Long assignedById);

    boolean isUserTaskCollaborator(Long taskId, Long userId);

    List<Long> getTasksForWhichUserIsCollaborator(Long userId);

    List<Long> getCollaboratorIdsByTaskId(Long taskId);

}
