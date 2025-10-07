package com.spmorangle.crm.taskmanagement.service.impl;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spmorangle.crm.taskmanagement.dto.CommentResponseDto;
import com.spmorangle.crm.taskmanagement.dto.CreateCommentDto;
import com.spmorangle.crm.taskmanagement.dto.CreateCommentResponseDto;
import com.spmorangle.crm.taskmanagement.dto.UpdateCommentDto;
import com.spmorangle.crm.taskmanagement.model.TaskComment;
import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.model.Subtask;
import com.spmorangle.crm.taskmanagement.repository.TaskCommentRepository;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import com.spmorangle.crm.taskmanagement.repository.TaskAssigneeRepository;
import com.spmorangle.crm.taskmanagement.repository.SubtaskRepository;
import com.spmorangle.crm.taskmanagement.service.CommentService;
import com.spmorangle.crm.taskmanagement.util.CommentPermissionHelper;
import com.spmorangle.crm.usermanagement.service.UserManagementService;
import com.spmorangle.crm.notification.messaging.publisher.NotificationMessagePublisher;
import com.spmorangle.crm.notification.messaging.dto.CommentNotificationMessageDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentPermissionHelper permissionHelper;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final SubtaskRepository subtaskRepository;
    private final UserManagementService userManagementService;
    private final NotificationMessagePublisher notificationPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateCommentResponseDto createComment(CreateCommentDto createCommentDto, Long currentUserId) {
        log.info("Creating comment for user: {} with taskId: {} and subtaskId: {} and parentCommentId: {}",
                 currentUserId, createCommentDto.getTaskId(), createCommentDto.getSubtaskId(), createCommentDto.getParentCommentId());

        validateCommentTarget(createCommentDto);

        TaskComment comment = new TaskComment();
        comment.setTaskId(createCommentDto.getTaskId());
        comment.setSubtaskId(createCommentDto.getSubtaskId());
        comment.setParentCommentId(createCommentDto.getParentCommentId());
        comment.setContent(createCommentDto.getContent());
        comment.setMentionedUserIds(createCommentDto.getMentionedUserIds());
        comment.setCreatedBy(currentUserId);
        comment.setCreatedAt(OffsetDateTime.now());

        // Log parent comment details if this is a reply
        if (createCommentDto.getParentCommentId() != null) {
            TaskComment parentComment = taskCommentRepository.findById(createCommentDto.getParentCommentId())
                    .orElse(null);
            if (parentComment != null) {
                log.info("Replying to comment ID: {} created by user: {} with content: '{}'",
                         parentComment.getId(), parentComment.getCreatedBy(),
                         parentComment.getContent().substring(0, Math.min(50, parentComment.getContent().length())));
            } else {
                log.warn("Parent comment ID: {} not found!", createCommentDto.getParentCommentId());
            }
        }

        Long projectId = getProjectIdFromTarget(createCommentDto);
        log.info("Retrieved projectId: {} for comment", projectId);
        comment.setProjectId(projectId);

        // Validate mentioned users
        validateMentionedUsers(createCommentDto.getMentionedUserIds(), projectId);

        TaskComment savedComment = taskCommentRepository.save(comment);
        log.info("Comment created with ID: {} - IsReply: {} - ParentID: {} - Content: '{}'",
                 savedComment.getId(),
                 savedComment.getParentCommentId() != null,
                 savedComment.getParentCommentId(),
                 savedComment.getContent().substring(0, Math.min(50, savedComment.getContent().length())));

        // Publish notification events via RabbitMQ
        try {
            String taskTitle = getTaskOrSubtaskTitle(createCommentDto.getTaskId(), createCommentDto.getSubtaskId());
            List<Long> taskAssigneeIds = getTaskAssigneeIds(createCommentDto.getTaskId(), createCommentDto.getSubtaskId());
            
            CommentNotificationMessageDto message;
            
            if (createCommentDto.getParentCommentId() != null) {
                // This is a reply - get parent comment author
                TaskComment parentComment = taskCommentRepository.findById(createCommentDto.getParentCommentId())
                        .orElse(null);
                Long parentAuthorId = parentComment != null ? parentComment.getCreatedBy() : null;
                
                message = CommentNotificationMessageDto.forCommentReply(
                    savedComment.getId(),
                    currentUserId,
                    savedComment.getContent(),
                    createCommentDto.getParentCommentId(),
                    parentAuthorId,
                    createCommentDto.getTaskId(),
                    taskTitle,
                    createCommentDto.getMentionedUserIds(),
                    taskAssigneeIds
                );
            } else {
                // Regular comment
                message = CommentNotificationMessageDto.forCommentCreated(
                    savedComment.getId(),
                    currentUserId,
                    savedComment.getContent(),
                    createCommentDto.getTaskId(),
                    taskTitle,
                    createCommentDto.getMentionedUserIds(),
                    taskAssigneeIds
                );
            }
            
            // Publish to RabbitMQ
            notificationPublisher.publishCommentNotification(message);
            log.info("Published notification message for comment ID: {}", savedComment.getId());
            
        } catch (Exception e) {
            log.error("Failed to publish notification for comment ID: {} - Error: {}", 
                     savedComment.getId(), e.getMessage(), e);
            // Don't fail comment creation if notification publishing fails
        }

        return CreateCommentResponseDto.builder()
                .id(savedComment.getId())
                .taskId(savedComment.getTaskId())
                .subtaskId(savedComment.getSubtaskId())
                .parentCommentId(savedComment.getParentCommentId())
                .content(savedComment.getContent())
                .authorId(savedComment.getCreatedBy())
                .createdAt(savedComment.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentResponseDto updateComment(UpdateCommentDto updateCommentDto, Long currentUserId) {
        log.info("Updating comment {} by user: {}", updateCommentDto.getCommentId(), currentUserId);

        TaskComment comment = taskCommentRepository.findById(updateCommentDto.getCommentId())
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!permissionHelper.canEditComment(currentUserId, comment)) {
            throw new RuntimeException("User not authorized to edit this comment");
        }

        if (comment.isDeleted()) {
            throw new RuntimeException("Cannot edit deleted comment");
        }

        comment.setContent(updateCommentDto.getContent());
        comment.setMentionedUserIds(updateCommentDto.getMentionedUserIds());
        comment.setEdited(true);
        comment.setUpdatedBy(currentUserId);
        comment.setUpdatedAt(OffsetDateTime.now());

        // Validate mentioned users
        validateMentionedUsers(updateCommentDto.getMentionedUserIds(), comment.getProjectId());

        TaskComment savedComment = taskCommentRepository.save(comment);


            
        // Handle mentions via RabbitMQ (only notify NEW mentions, don't spam assignees)
        try {
            String taskTitle = getTaskOrSubtaskTitle(comment.getTaskId(), comment.getSubtaskId());
            
            // Compare old vs new mentions to only notify NEW mentions
            Set<Long> oldMentions = new HashSet<>(comment.getMentionedUserIds() != null ? 
                comment.getMentionedUserIds() : List.of());
            Set<Long> newMentions = new HashSet<>(updateCommentDto.getMentionedUserIds() != null ? 
                updateCommentDto.getMentionedUserIds() : List.of());

            // Find newly added mentions
            Set<Long> addedMentions = new HashSet<>(newMentions);
            addedMentions.removeAll(oldMentions);

            // Only notify newly mentioned users via RabbitMQ
            if (!addedMentions.isEmpty()) {
                CommentNotificationMessageDto message = CommentNotificationMessageDto.forMention(
                    savedComment.getId(),
                    currentUserId,
                    savedComment.getContent(),
                    comment.getTaskId(),
                    taskTitle,
                    addedMentions.stream().toList() // Convert to List
                );
                
                notificationPublisher.publishCommentNotification(message);
                log.info("Published mention notification for comment edit ID: {} with {} new mentions", 
                        savedComment.getId(), addedMentions.size());
            }
            
        } catch (Exception e) {
            log.error("Failed to publish mention notification for comment edit ID: {} - Error: {}", 
                     savedComment.getId(), e.getMessage(), e);
            // Don't fail comment update if notification publishing fails
        }

        return mapToCommentResponseDto(savedComment, currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long currentUserId) {
        log.info("Deleting comment {} by user: {}", commentId, currentUserId);

        TaskComment comment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!permissionHelper.canDeleteComment(currentUserId, comment)) {
            throw new RuntimeException("User not authorized to delete this comment");
        }

        comment.setDeleted(true);
        comment.setUpdatedBy(currentUserId);
        comment.setUpdatedAt(OffsetDateTime.now());

        taskCommentRepository.save(comment);
    }

    @Override
    public List<CommentResponseDto> getTaskComments(Long taskId, Long currentUserId) {
        log.info("Getting comments for task: {} with user context: {}", taskId, currentUserId);
        List<TaskComment> topLevelComments = taskCommentRepository.findTopLevelCommentsByTaskId(taskId);
        return buildCommentTree(topLevelComments, currentUserId);
    }

    @Override
    public List<CommentResponseDto> getSubtaskComments(Long subtaskId, Long currentUserId) {
        log.info("Getting comments for subtask: {} with user context: {}", subtaskId, currentUserId);
        List<TaskComment> topLevelComments = taskCommentRepository.findTopLevelCommentsBySubtaskId(subtaskId);
        return buildCommentTree(topLevelComments, currentUserId);
    }

    @Override
    public List<CommentResponseDto> getCommentReplies(Long parentCommentId, Long currentUserId) {
        log.info("Getting replies for comment: {} with user context: {}", parentCommentId, currentUserId);
        List<TaskComment> replies = taskCommentRepository.findRepliesByParentCommentId(parentCommentId);
        return replies.stream()
                .map(reply -> mapToCommentResponseDto(reply, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    public CommentResponseDto getCommentById(Long commentId, Long currentUserId) {
        log.info("Getting comment by ID: {} with user context: {}", commentId, currentUserId);
        TaskComment comment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        return mapToCommentResponseDto(comment, currentUserId);
    }

    @Override
    public List<CommentResponseDto> getUserMentions(Long userId) {
        log.info("Getting mentions for user: {}", userId);
        List<TaskComment> mentions = taskCommentRepository.findByMentionedUserId(userId);
        return mentions.stream()
                .map(mention -> mapToCommentResponseDto(mention, userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentResponseDto> getTaskCommentsWithFilters(Long taskId, Long authorId, Boolean isResolved, Long currentUserId) {
        log.info("Getting filtered comments for task: {} by user: {}", taskId, currentUserId);
        
        // Security check - ensure user can access this task's comments
        if (!permissionHelper.canReadComments(currentUserId, taskId)) {
            throw new RuntimeException("User not authorized to read task comments");
        }
        
        List<TaskComment> topLevelComments = taskCommentRepository.findTaskCommentsWithFilters(taskId, authorId, isResolved)
                .stream()
                .filter(comment -> comment.getParentCommentId() == null)
                .collect(Collectors.toList());
        return buildCommentTree(topLevelComments, currentUserId);
    }

    @Override
    public List<CommentResponseDto> getSubtaskCommentsWithFilters(Long subtaskId, Long authorId, Boolean isResolved, Long currentUserId) {
        log.info("Getting filtered comments for subtask: {} by user: {}", subtaskId, currentUserId);
        
        // Security check - get subtask's parent task and check permissions
        Subtask subtask = subtaskRepository.findById(subtaskId)
            .orElseThrow(() -> new RuntimeException("Subtask not found with ID: " + subtaskId));
        
        if (!permissionHelper.canReadComments(currentUserId, subtask.getTaskId())) {
            throw new RuntimeException("User not authorized to read subtask comments");
        }
        
        List<TaskComment> topLevelComments = taskCommentRepository.findSubtaskCommentsWithFilters(subtaskId, authorId, isResolved)
                .stream()
                .filter(comment -> comment.getParentCommentId() == null)
                .collect(Collectors.toList());
        return buildCommentTree(topLevelComments, currentUserId);
    }

    @Override
    public List<Long> getCommentAuthorsByTaskId(Long taskId) {
        log.info("Getting comment authors for task: {}", taskId);
        return taskCommentRepository.findCommentAuthorsByTaskId(taskId);
    }

    @Override
    public List<Long> getCommentAuthorsBySubtaskId(Long subtaskId) {
        log.info("Getting comment authors for subtask: {}", subtaskId);
        return taskCommentRepository.findCommentAuthorsBySubtaskId(subtaskId);
    }

    // PermissionAware interface implementation
    @Override
    public boolean canRead(Long userId, Long resourceId) {
        return permissionHelper.canReadComments(userId, resourceId);
    }

    @Override
    public boolean canWrite(Long userId, Long resourceId) {
        return permissionHelper.canReplyToComment(userId, resourceId);
    }

    @Override
    public boolean canModerate(Long userId, Long resourceId) {
        return permissionHelper.canModerateComment(userId, resourceId);
    }

    private void validateCommentTarget(CreateCommentDto createCommentDto) {
        if (createCommentDto.getTaskId() == null && createCommentDto.getSubtaskId() == null) {
            throw new RuntimeException("Comment must be associated with either a task or subtask");
        }
        if (createCommentDto.getTaskId() != null && createCommentDto.getSubtaskId() != null) {
            throw new RuntimeException("Comment cannot be associated with both task and subtask");
        }
    }

    private Long getProjectIdFromTarget(CreateCommentDto createCommentDto) {
        log.debug("Getting project ID from target: taskId={}, subtaskId={}", 
                  createCommentDto.getTaskId(), createCommentDto.getSubtaskId());
        
        if (createCommentDto.getTaskId() != null) {
            log.debug("Looking up task with ID: {}", createCommentDto.getTaskId());
            Task task = taskRepository.findById(createCommentDto.getTaskId())
                    .orElseThrow(() -> new RuntimeException("Task not found with ID: " + createCommentDto.getTaskId()));
            return task.getProjectId();
        } else {
            log.debug("Looking up subtask with ID: {}", createCommentDto.getSubtaskId());
            return subtaskRepository.findById(createCommentDto.getSubtaskId())
                    .map(subtask -> {
                        log.debug("Found subtask with projectId: {}", subtask.getProjectId());
                        return subtask.getProjectId();
                    })
                    .orElseThrow(() -> new RuntimeException("Subtask not found with ID: " + createCommentDto.getSubtaskId()));
        }
    }

    private List<CommentResponseDto> buildCommentTree(List<TaskComment> topLevelComments, Long currentUserId) {
        return topLevelComments.stream()
                .map(comment -> buildCommentDto(comment, currentUserId))
                .collect(Collectors.toList());
    }

    private CommentResponseDto buildCommentDto(TaskComment comment, Long currentUserId) {
        log.debug("Building comment DTO for comment ID: {} (parentId: {})", comment.getId(), comment.getParentCommentId());
        CommentResponseDto commentDto = mapToCommentResponseDto(comment, currentUserId);

        // Recursively build replies
        List<TaskComment> replies = taskCommentRepository.findRepliesByParentCommentId(comment.getId());
        log.debug("Comment ID: {} has {} direct replies", comment.getId(), replies.size());

        List<CommentResponseDto> replyDtos = replies.stream()
                .map(reply -> {
                    log.debug("Processing reply ID: {} for parent ID: {}", reply.getId(), comment.getId());
                    return buildCommentDto(reply, currentUserId);
                })
                .collect(Collectors.toList());

        log.debug("Built comment ID: {} with {} nested replies", comment.getId(), replyDtos.size());

        return CommentResponseDto.builder()
                .id(commentDto.getId())
                .taskId(commentDto.getTaskId())
                .subtaskId(commentDto.getSubtaskId())
                .projectId(commentDto.getProjectId())
                .parentCommentId(commentDto.getParentCommentId())
                .content(commentDto.getContent())
                .mentionedUserIds(commentDto.getMentionedUserIds())
                .isEdited(commentDto.isEdited())
                .isDeleted(commentDto.isDeleted())
                .authorId(commentDto.getAuthorId())
                .authorUsername(commentDto.getAuthorUsername())
                .createdAt(commentDto.getCreatedAt())
                .updatedAt(commentDto.getUpdatedAt())
                .replies(replyDtos)
                .replyCount(replyDtos.size())
                .canEdit(commentDto.isCanEdit())
                .canDelete(commentDto.isCanDelete())
                .canReply(commentDto.isCanReply())
                .canModerate(commentDto.isCanModerate())
                .build();
    }

    private CommentResponseDto mapToCommentResponseDto(TaskComment comment, Long currentUserId) {
        String authorUsername = null;
        try {
            authorUsername = userManagementService.getUserById(comment.getCreatedBy()).username();
        } catch (Exception e) {
            log.warn("Could not fetch username for user {}: {}", comment.getCreatedBy(), e.getMessage());
            authorUsername = "Unknown User";
        }

        long replyCount = taskCommentRepository.countRepliesByParentCommentId(comment.getId());

        // Default permission flags when currentUserId is not provided
        boolean canEdit = false;
        boolean canDelete = false;
        boolean canReply = true; // Default to allow replies
        boolean canModerate = false;

        // Calculate actual permission flags if currentUserId is provided
        if (currentUserId != null) {
            canEdit = permissionHelper.canEditComment(currentUserId, comment);
            canDelete = permissionHelper.canDeleteComment(currentUserId, comment);
            canReply = permissionHelper.canReplyToComment(currentUserId, comment.getProjectId());
            canModerate = permissionHelper.canModerateComment(currentUserId, comment.getProjectId());
        }

        return CommentResponseDto.builder()
                .id(comment.getId())
                .taskId(comment.getTaskId())
                .subtaskId(comment.getSubtaskId())
                .projectId(comment.getProjectId())
                .parentCommentId(comment.getParentCommentId())
                .content(comment.getContent())
                .mentionedUserIds(comment.getMentionedUserIds())
                .isEdited(comment.isEdited())
                .isDeleted(comment.isDeleted())
                .authorId(comment.getCreatedBy())
                .authorUsername(authorUsername)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .replies(List.of()) // Will be populated by buildCommentTree if needed
                .replyCount((int) replyCount)
                .canEdit(canEdit)
                .canDelete(canDelete)
                .canReply(canReply)
                .canModerate(canModerate)
                .build();
    }

    private void validateMentionedUsers(List<Long> mentionedUserIds, Long projectId) {
        if (mentionedUserIds == null || mentionedUserIds.isEmpty()) {
            return; // No mentions to validate
        }

        log.debug("Validating mentioned users: {} for project: {}", mentionedUserIds, projectId);

        // Get all project members
        List<Long> projectMemberIds = userManagementService.getProjectMembers(projectId)
                .stream()
                .map(user -> user.id())
                .toList();

        // Check if all mentioned users are project members
        List<Long> invalidUserIds = mentionedUserIds.stream()
                .filter(userId -> !projectMemberIds.contains(userId))
                .toList();

        if (!invalidUserIds.isEmpty()) {
            log.warn("Invalid mentioned user IDs found: {} for project: {}", invalidUserIds, projectId);
            throw new RuntimeException("Cannot mention users who are not members of the project: " + invalidUserIds);
        }

        log.debug("All mentioned users are valid project members");
    }

    private List<Long> getTaskAssigneeIds(Long taskId, Long subtaskId) {
        if (taskId != null) {
            // Get task assignees directly
            return taskAssigneeRepository.findAssigneeIdsByTaskId(taskId);
        } else if (subtaskId != null) {
            // Get parent task assignees (since subtasks don't have separate assignees)
            Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new RuntimeException("Subtask not found with ID: " + subtaskId));
            return taskAssigneeRepository.findAssigneeIdsByTaskId(subtask.getTaskId());
        }
        return List.of(); // Return empty list if neither taskId nor subtaskId is provided
    }

    private String getTaskOrSubtaskTitle(Long taskId, Long subtaskId) {
        if (taskId != null) {
            return taskRepository.findById(taskId)
                .map(Task::getTitle)
                .orElse("Unknown Task");
        } else if (subtaskId != null) {
            return subtaskRepository.findById(subtaskId)
                .map(Subtask::getTitle)
                .orElse("Unknown Subtask");
        }
        return "Unknown Item";
    }
}