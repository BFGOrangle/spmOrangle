package com.spmorangle.crm.taskmanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spmorangle.common.model.User;
import com.spmorangle.common.service.UserContextService;
import com.spmorangle.crm.taskmanagement.dto.CreateSubtaskDto;
import com.spmorangle.crm.taskmanagement.dto.SubtaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.UpdateSubtaskDto;
import com.spmorangle.crm.taskmanagement.service.SubtaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/subtasks")
@RequiredArgsConstructor
public class SubtaskController {

    private final SubtaskService subtaskService;
    private final UserContextService userContextService;

    /**
     * Create a new subtask
     * @param createSubtaskDto
     * @return SubtaskResponseDto
     */
    @PostMapping
    public ResponseEntity<SubtaskResponseDto> createSubtask(
            @Valid @RequestBody CreateSubtaskDto createSubtaskDto) {
        User user = userContextService.getRequestingUser();
        log.info("Creating subtask for task {} by user {}", createSubtaskDto.getTaskId(), user.getId());
        SubtaskResponseDto response = subtaskService.createSubtask(createSubtaskDto, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a subtask by id
     * @param subtaskId
     * @return SubtaskResponseDto
     */
    @GetMapping("/{subtaskId}")
    public ResponseEntity<SubtaskResponseDto> getSubtask(
            @PathVariable Long subtaskId) {
        log.info("Getting subtask: {}", subtaskId);
        SubtaskResponseDto subtask = subtaskService.getSubtaskById(subtaskId);
        return ResponseEntity.ok(subtask);
    }

    /**
     * Get subtasks by task id
     * @param taskId
     * @return List<SubtaskResponseDto>
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<SubtaskResponseDto>> getSubtasksByTask(
            @PathVariable Long taskId) {
        log.info("Getting subtasks for task: {}", taskId);
        List<SubtaskResponseDto> subtasks = subtaskService.getSubtasksByTaskId(taskId);
        return ResponseEntity.ok(subtasks);
    }

    /**
     * Get subtasks by project id
     * @param projectId
     * @return List<SubtaskResponseDto>
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<SubtaskResponseDto>> getSubtasksByProject(
            @PathVariable Long projectId) {
        log.info("Getting subtasks for project: {}", projectId);
        List<SubtaskResponseDto> subtasks = subtaskService.getSubtasksByProjectId(projectId);
        return ResponseEntity.ok(subtasks);
    }

    /**
     * Update a subtask
     * @param subtaskId
     * @param updateSubtaskDto
     * @return SubtaskResponseDto
     */
    @PutMapping("/{subtaskId}")
    public ResponseEntity<SubtaskResponseDto> updateSubtask(
            @PathVariable Long subtaskId,
            @Valid @RequestBody UpdateSubtaskDto updateSubtaskDto) {
        User user = userContextService.getRequestingUser();
        log.info("Updating subtask: {} by user: {}", subtaskId, user.getId());
        
        SubtaskResponseDto response = subtaskService.updateSubtask(subtaskId, updateSubtaskDto, user.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a subtask
     * @param subtaskId
     * @return Void
     */
    @DeleteMapping("/{subtaskId}")
    public ResponseEntity<Void> deleteSubtask(
            @PathVariable Long subtaskId) {
        User user = userContextService.getRequestingUser();
        log.info("Deleting subtask: {} by user: {}", subtaskId, user.getId());
        subtaskService.deleteSubtask(subtaskId, user.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
