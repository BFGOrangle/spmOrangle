package com.spmorangle.crm.taskmanagement.controller;

import com.spmorangle.common.model.User;
import com.spmorangle.common.service.UserContextService;
import com.spmorangle.crm.taskmanagement.dto.*;
import com.spmorangle.crm.taskmanagement.service.CollaboratorService;
import com.spmorangle.crm.taskmanagement.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskManagementController {

    private final CollaboratorService collaboratorService;
    private final TaskService taskService;
    private final UserContextService userContextService;

    @GetMapping
    public ResponseEntity<List<GetTaskResponseDto>> getTasks(){
        User user = userContextService.getRequestingUser();
        log.info("Fetching tasks for user {}", user.getId());
        List<GetTaskResponseDto> response = taskService.getTasks(user.getId());

        return ResponseEntity.status(HttpStatus.FOUND).body(response);
    }

    @PostMapping
    public ResponseEntity<CreateTaskResponseDto> createTask(
            @Valid @RequestBody CreateTaskDto createTaskDto) {
        
        log.info("Creating task for user {}", createTaskDto.getOwnerId());
        
        CreateTaskResponseDto response = taskService.createTask(createTaskDto, createTaskDto.getOwnerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/collaborator")
    public ResponseEntity<AddCollaboratorResponseDto> addCollaborator(
            @Valid
            @RequestBody AddCollaboratorRequestDto addCollaboratorRequestDto) {
        log.info("Adding collaborator {} to task {}", addCollaboratorRequestDto.getCollaboratorId(), addCollaboratorRequestDto.getTaskId());
        AddCollaboratorResponseDto response = collaboratorService.addCollaborator(addCollaboratorRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/collaborator")
    public ResponseEntity<Void> removeCollaborator(
            @Valid
            @RequestBody RemoveCollaboratorRequestDto removeCollaboratorRequestDto) {
        log.info("Removing collaborator {} from task {}", removeCollaboratorRequestDto.getCollaboratorId(), removeCollaboratorRequestDto.getTaskId());
        collaboratorService.removeCollaborator(removeCollaboratorRequestDto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
