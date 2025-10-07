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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spmorangle.common.model.User;
import com.spmorangle.common.service.UserContextService;
import com.spmorangle.crm.taskmanagement.dto.CommentResponseDto;
import com.spmorangle.crm.taskmanagement.dto.CreateSubtaskDto;
import com.spmorangle.crm.taskmanagement.dto.SubtaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.UpdateSubtaskDto;
import com.spmorangle.crm.taskmanagement.service.CommentService;
import com.spmorangle.crm.taskmanagement.service.SubtaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/subtasks")
@RequiredArgsConstructor
public class SubtaskController {

    private final CommentService commentService;
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
        User user = userContextService.getRequestingUser();
        log.info("Getting subtask: {} for user: {}", subtaskId, user.getId());
        SubtaskResponseDto subtask = subtaskService.getSubtaskById(subtaskId, user.getId());
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
        User user = userContextService.getRequestingUser();
        log.info("Getting subtasks for task: {} for user: {}", taskId, user.getId());
        List<SubtaskResponseDto> subtasks = subtaskService.getSubtasksByTaskId(taskId, user.getId());
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
        User user = userContextService.getRequestingUser();
        log.info("Getting subtasks for project: {} for user: {}", projectId, user.getId());
        List<SubtaskResponseDto> subtasks = subtaskService.getSubtasksByProjectId(projectId, user.getId());
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

    /**
     * Get comments for a subtask with filtering support
     * @param subtaskId
     * @param authorId
     * @param resolved
     * @param filter
     * @return List<CommentResponseDto>
     */
    @GetMapping("/{subtaskId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getSubtaskComments(
            @PathVariable Long subtaskId,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Boolean resolved,
            @RequestParam(defaultValue = "ALL") String filter) {

        User user = userContextService.getRequestingUser();

        // Future: Check read permissions
        // if (!commentService.canRead(user.getId(), subtaskId)) {
        //     throw new AccessDeniedException("No permission to read comments");
        // }

        log.info("Getting comments for subtask: {} with filter: {}", subtaskId, filter);

        List<CommentResponseDto> comments;
        if ("ALL".equals(filter)) {
            comments = commentService.getSubtaskComments(subtaskId, user.getId());
        } else {
            comments = commentService.getSubtaskCommentsWithFilters(subtaskId, authorId, resolved);
        }

        return ResponseEntity.ok(comments);
    }
}
