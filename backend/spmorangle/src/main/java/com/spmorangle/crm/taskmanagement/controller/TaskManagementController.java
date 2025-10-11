package com.spmorangle.crm.taskmanagement.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.spmorangle.common.model.User;
import com.spmorangle.common.service.UserContextService;
import com.spmorangle.crm.taskmanagement.dto.*;
import com.spmorangle.crm.taskmanagement.service.CollaboratorService;
import com.spmorangle.crm.taskmanagement.service.CommentService;
import com.spmorangle.crm.taskmanagement.service.TaskService;
import com.spmorangle.crm.usermanagement.dto.UserResponseDto;
import com.spmorangle.crm.usermanagement.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final CommentService commentService;
    private final TaskService taskService;
    private final UserContextService userContextService;
    private final UserManagementService userManagementService;


    /**
     * Create a new task
     * @param createTaskDto
     * @return CreateTaskResponseDto
     */
    @PostMapping
    public ResponseEntity<CreateTaskResponseDto> createTask(
            @Valid @RequestBody CreateTaskDto createTaskDto) {
        User user = userContextService.getRequestingUser();
        CreateTaskResponseDto response = taskService.createTask(createTaskDto, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/with-owner-id")
    public ResponseEntity<CreateTaskResponseDto> createTaskWithSpecifiedOwner(
            @Valid @RequestBody CreateTaskDto createTaskDto) {
        User user = userContextService.getRequestingUser();

        Long specifiedOwnerId = Optional.ofNullable(createTaskDto.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("Specific Owner ID must be provided"));

        log.info("Creating task for user {}", user.getId());
        CreateTaskResponseDto response = taskService.createTask(createTaskDto, specifiedOwnerId, user.getId());
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

    @GetMapping("/collaborators")
    public ResponseEntity<List<UserResponseDto>> getCollaborators(){
        List<UserResponseDto> collaborators = userManagementService.getCollaborators();
        return ResponseEntity.status(HttpStatus.OK).body(collaborators);
    }

    /**
     * Get tasks for a project
     * @param projectId
     * @return List<TaskResponseDto>
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskResponseDto>> getProjectTasks(
            @PathVariable Long projectId) {
        User user = userContextService.getRequestingUser();
        log.info("Getting tasks for project: {}", projectId);
        List<TaskResponseDto> tasks = taskService.getProjectTasks(user.getId(), projectId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long taskId) {
        User user = userContextService.getRequestingUser();
        log.info("Getting task by ID: {}", taskId);
        TaskResponseDto task = taskService.getTaskById(taskId, user.getId());
        return ResponseEntity.ok(task);
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
     * Update a task
     * @param updateTaskDto
     * @return UpdateTaskResponseDto
     */
    @PutMapping
    public ResponseEntity<UpdateTaskResponseDto> updateTask(
            @Valid @RequestBody UpdateTaskDto updateTaskDto) {
        User user = userContextService.getRequestingUser();
        log.info("Updating task: {} by user: {}", updateTaskDto.getTaskId(), user.getId());
        UpdateTaskResponseDto response = taskService.updateTask(updateTaskDto, user.getId());
        return ResponseEntity.ok(response);
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

    /**
     * Create a comment on a task or subtask
     * @param createCommentDto
     * @return CreateCommentResponseDto
     */
    @PostMapping("/comments")
    public ResponseEntity<CreateCommentResponseDto> createComment(
            @Valid @RequestBody CreateCommentDto createCommentDto) {
        User user = userContextService.getRequestingUser();
        log.info("Creating comment by user: {}", user.getId());
        CreateCommentResponseDto response = commentService.createComment(createCommentDto, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update a comment
     * @param updateCommentDto
     * @return CommentResponseDto
     */
    @PutMapping("/comments")
    public ResponseEntity<CommentResponseDto> updateComment(
            @Valid @RequestBody UpdateCommentDto updateCommentDto) {
        User user = userContextService.getRequestingUser();
        log.info("Updating comment {} by user: {}", updateCommentDto.getCommentId(), user.getId());
        CommentResponseDto response = commentService.updateComment(updateCommentDto, user.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a comment
     * @param commentId
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId) {
        User user = userContextService.getRequestingUser();
        log.info("Deleting comment {} by user: {}", commentId, user.getId());
        commentService.deleteComment(commentId, user.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Get comments for a task with filtering support
     * @param taskId
     * @param authorId
     * @param resolved
     * @param filter
     * @return List<CommentResponseDto>
     */
    @GetMapping("/{taskId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getTaskComments(
            @PathVariable Long taskId,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Boolean resolved,
            @RequestParam(defaultValue = "ALL") String filter) {

        User user = userContextService.getRequestingUser();

        // Future: Check read permissions
        // if (!commentService.canRead(user.getId(), taskId)) {
        //     throw new AccessDeniedException("No permission to read comments");
        // }

        log.info("Getting comments for task: {} with filter: {}", taskId, filter);

        List<CommentResponseDto> comments;
        if ("ALL".equals(filter)) {
            comments = commentService.getTaskComments(taskId, user.getId());
        } else {
            comments = commentService.getTaskCommentsWithFilters(taskId, authorId, resolved, user.getId());
        }

        return ResponseEntity.ok(comments);
    }

    /**
     * Get replies for a comment
     * @param commentId
     * @return List<CommentResponseDto>
     */
    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<List<CommentResponseDto>> getCommentReplies(
            @PathVariable Long commentId) {
        User user = userContextService.getRequestingUser();
        log.info("Getting replies for comment: {}", commentId);
        List<CommentResponseDto> replies = commentService.getCommentReplies(commentId, user.getId());
        return ResponseEntity.ok(replies);
    }

    /**
     * Get comment by ID
     * @param commentId
     * @return CommentResponseDto
     */
    @GetMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> getCommentById(
            @PathVariable Long commentId) {
        User user = userContextService.getRequestingUser();
        log.info("Getting comment by ID: {}", commentId);
        CommentResponseDto comment = commentService.getCommentById(commentId, user.getId());
        return ResponseEntity.ok(comment);
    }

    /**
     * Get user mentions
     * @return List<CommentResponseDto>
     */
    @GetMapping("/comments/mentions")
    public ResponseEntity<List<CommentResponseDto>> getUserMentions() {
        User user = userContextService.getRequestingUser();
        log.info("Getting mentions for user: {}", user.getId());
        List<CommentResponseDto> mentions = commentService.getUserMentions(user.getId());
        return ResponseEntity.ok(mentions);
    }

    /**
     * Get list of comment authors for a task (for filtering UI)
     * @param taskId
     * @return List<UserResponseDto>
     */
    @GetMapping("/{taskId}/comments/authors")
    public ResponseEntity<List<UserResponseDto>> getTaskCommentAuthors(@PathVariable Long taskId) {
        log.info("Getting comment authors for task: {}", taskId);
        List<Long> authorIds = commentService.getCommentAuthorsByTaskId(taskId);
        List<UserResponseDto> authors = authorIds.stream()
                .map(id -> {
                    try {
                        return userManagementService.getUserById(id);
                    } catch (Exception e) {
                        log.warn("Could not fetch user {}: {}", id, e.getMessage());
                        return null;
                    }
                })
                .filter(author -> author != null)
                .collect(Collectors.toList());
        return ResponseEntity.ok(authors);
    }
}
