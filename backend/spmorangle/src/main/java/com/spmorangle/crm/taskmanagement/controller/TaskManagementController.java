package com.spmorangle.crm.taskmanagement.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskManagementController {

    private final CollaboratorService collaboratorService;
    private final TaskService taskService;
    private final UserContextService userContextService;


    /**
     * Create a new task
     * @param createTaskDto
     * @return CreateTaskResponseDto
     */
    @PostMapping
    public ResponseEntity<CreateTaskResponseDto> createTask(
            @Valid @RequestBody CreateTaskDto createTaskDto) {
        User user = userContextService.getRequestingUser();
        log.info("Creating task for user {}", user.getId());
        CreateTaskResponseDto response = taskService.createTask(createTaskDto, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Add a collaborator to a task
     * @param addCollaboratorRequestDto
     * @return AddCollaboratorResponseDto
     */
    @PostMapping("/collaborator")
    public ResponseEntity<AddCollaboratorResponseDto> addCollaborator(
            @Valid
            @RequestBody AddCollaboratorRequestDto addCollaboratorRequestDto) {
        log.info("Adding collaborator {} to task {}", addCollaboratorRequestDto.getCollaboratorId(), addCollaboratorRequestDto.getTaskId());
        AddCollaboratorResponseDto response = collaboratorService.addCollaborator(addCollaboratorRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Remove a collaborator from a task
     * @param removeCollaboratorRequestDto
     */
    @DeleteMapping("/collaborator")
    public ResponseEntity<Void> removeCollaborator(
            @Valid
            @RequestBody RemoveCollaboratorRequestDto removeCollaboratorRequestDto) {
        log.info("Removing collaborator {} from task {}", removeCollaboratorRequestDto.getCollaboratorId(), removeCollaboratorRequestDto.getTaskId());
        collaboratorService.removeCollaborator(removeCollaboratorRequestDto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Get tasks for a project
     * @param projectId
     * @return List<TaskResponseDto>
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskResponseDto>> getProjectTasks(
            @PathVariable Long projectId) {
        log.info("Getting tasks for project: {}", projectId);
        List<TaskResponseDto> tasks = taskService.getProjectTasks(projectId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Get personal tasks
     * @return List<TaskResponseDto>
     */
    @GetMapping("/personal")
    public ResponseEntity<List<TaskResponseDto>> getPersonalTasks() {
        User user = userContextService.getRequestingUser();
        log.info("Fetching tasks for user {}", user.getId());
        List<TaskResponseDto> tasks = taskService.getPersonalTasks(user.getId());
        return ResponseEntity.ok(tasks);
    }

    /**
     * Get all tasks for a user
     * @return List<TaskResponseDto>
     */
    @GetMapping("/user")
    public ResponseEntity<List<TaskResponseDto>> getAllUserTasks() {
        User user = userContextService.getRequestingUser();
        log.info("Getting all tasks for user: {}", user.getId());
        List<TaskResponseDto> tasks = taskService.getAllUserTasks(user.getId());
        return ResponseEntity.ok(tasks);
    }

    /**
     * Delete a task
     * @param taskId
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long taskId) {
        User user = userContextService.getRequestingUser();
        log.info("Deleting task: {} by user: {}", taskId, user.getId());
        taskService.deleteTask(taskId, user.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
