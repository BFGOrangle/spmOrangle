package com.spmorangle.crm.taskmanagement.controller;

import java.util.List;

import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorRequestDto;
import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorResponseDto;
import com.spmorangle.crm.taskmanagement.dto.RemoveCollaboratorRequestDto;
import com.spmorangle.crm.taskmanagement.dto.CreateTaskDto;
import com.spmorangle.crm.taskmanagement.dto.CreateTaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.TaskResponseDto;
import com.spmorangle.crm.taskmanagement.service.CollaboratorService;
import com.spmorangle.crm.taskmanagement.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskManagementController {

    private final CollaboratorService collaboratorService;
    private final TaskService taskService;

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

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskResponseDto>> getProjectTasks(
            @PathVariable Long projectId) {
        log.info("Getting tasks for project: {}", projectId);
        List<TaskResponseDto> tasks = taskService.getProjectTasks(projectId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/personal")
    public ResponseEntity<List<TaskResponseDto>> getPersonalTasks(
            @RequestParam Long userId) {
        log.info("Getting personal tasks for user: {}", userId);
        List<TaskResponseDto> tasks = taskService.getPersonalTasks(userId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/user")
    public ResponseEntity<List<TaskResponseDto>> getAllUserTasks(
            @RequestParam Long userId) {
        log.info("Getting all tasks for user: {}", userId);
        List<TaskResponseDto> tasks = taskService.getAllUserTasks(userId);
        return ResponseEntity.ok(tasks);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long taskId,
            @RequestParam Long currentUserId) {
        log.info("Deleting task: {} by user: {}", taskId, currentUserId);
        taskService.deleteTask(taskId, currentUserId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
