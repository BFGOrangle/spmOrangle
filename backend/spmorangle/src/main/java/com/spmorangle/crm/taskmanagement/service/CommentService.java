package com.spmorangle.crm.taskmanagement.service;

import java.util.List;

import com.spmorangle.crm.taskmanagement.dto.CommentResponseDto;
import com.spmorangle.crm.taskmanagement.dto.CreateCommentDto;
import com.spmorangle.crm.taskmanagement.dto.CreateCommentResponseDto;
import com.spmorangle.crm.taskmanagement.dto.UpdateCommentDto;

public interface CommentService extends PermissionAware {

    CreateCommentResponseDto createComment(CreateCommentDto createCommentDto, Long currentUserId);

    CommentResponseDto updateComment(UpdateCommentDto updateCommentDto, Long currentUserId);

    void deleteComment(Long commentId, Long currentUserId);

    List<CommentResponseDto> getTaskComments(Long taskId);

    List<CommentResponseDto> getSubtaskComments(Long subtaskId);

    List<CommentResponseDto> getCommentReplies(Long parentCommentId);

    CommentResponseDto getCommentById(Long commentId);

    // Overloaded methods with current user context for proper permissions
    List<CommentResponseDto> getTaskComments(Long taskId, Long currentUserId);

    List<CommentResponseDto> getSubtaskComments(Long subtaskId, Long currentUserId);

    List<CommentResponseDto> getCommentReplies(Long parentCommentId, Long currentUserId);

    CommentResponseDto getCommentById(Long commentId, Long currentUserId);

    List<CommentResponseDto> getUserMentions(Long userId);

    List<CommentResponseDto> getTaskCommentsWithFilters(Long taskId, Long authorId, Boolean isResolved);

    List<CommentResponseDto> getSubtaskCommentsWithFilters(Long subtaskId, Long authorId, Boolean isResolved);

    List<Long> getCommentAuthorsByTaskId(Long taskId);

    List<Long> getCommentAuthorsBySubtaskId(Long subtaskId);
}