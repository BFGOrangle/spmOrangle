package com.spmorangle.crm.taskmanagement.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.spmorangle.crm.taskmanagement.model.TaskComment;

@Component
public class CommentPermissionHelper {

    @Value("${app.features.permissions.enabled:false}")
    private boolean permissionsEnabled;

    public boolean canEditComment(Long userId, TaskComment comment) {
        if (!permissionsEnabled) {
            // Fallback: only author can edit
            return comment.getCreatedBy().equals(userId);
        }
        // Future: delegate to permission service
        // return permissionService.canEdit(userId, comment);
        return comment.getCreatedBy().equals(userId);
    }

    public boolean canDeleteComment(Long userId, TaskComment comment) {
        if (!permissionsEnabled) {
            // Fallback: only author can delete
            return comment.getCreatedBy().equals(userId);
        }
        // Future: delegate to permission service
        return comment.getCreatedBy().equals(userId);
    }

    public boolean canModerateComment(Long userId, Long projectId) {
        if (!permissionsEnabled) {
            // Fallback: no moderation
            return false;
        }
        // Future: check if user is manager/admin
        return false;
    }

    public boolean canReplyToComment(Long userId, Long projectId) {
        if (!permissionsEnabled) {
            // Fallback: all authenticated users can reply
            return true;
        }
        // Future: check if user has write access to project
        return true;
    }

    public boolean canReadComments(Long userId, Long projectId) {
        if (!permissionsEnabled) {
            // Fallback: all authenticated users can read
            return true;
        }
        // Future: check if user has read access to project
        return true;
    }
}