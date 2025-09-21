package com.spmorangle.crm.taskmanagement.service;

import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorRequestDto;
import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorResponseDto;
import com.spmorangle.crm.taskmanagement.dto.RemoveCollaboratorRequestDto;

public interface CollaboratorService {

    AddCollaboratorResponseDto addCollaborator(AddCollaboratorRequestDto requestDto);

    void removeCollaborator(RemoveCollaboratorRequestDto requestDto);
}
