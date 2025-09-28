package com.spmorangle.crm.taskmanagement.model;

import java.time.OffsetDateTime;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "task_comments", schema = "syncup")
public class TaskComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "task_id", nullable = true)
    private Long taskId;

    @Column(name = "subtask_id", nullable = true)
    private Long subtaskId;

    @NotNull
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "parent_comment_id", nullable = true)
    private Long parentCommentId;

    @NotNull
    @Size(max = 2000, message = "Comment content cannot exceed 2000 characters")
    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @Column(name = "mentioned_user_id", nullable = true)
    private Long mentionedUserId;

    @Column(name = "mentioned_user_ids")
    private List<Long> mentionedUserIds;

    @Column(name = "is_edited", nullable = false)
    private boolean isEdited = false;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "is_resolved", nullable = false)
    private boolean isResolved = false;

    @Column(name = "resolved_by", nullable = true)
    private Long resolvedBy;

    @Column(name = "resolved_at", nullable = true)
    private OffsetDateTime resolvedAt;

    @Column(name = "priority", nullable = true)
    private String priority = "NORMAL";

    @NotNull
    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    @Column(name = "updated_by", nullable = true)
    private Long updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @ColumnDefault("now()")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = true)
    @ColumnDefault("now()")
    private OffsetDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id", insertable = false, updatable = false)
    private TaskComment parentComment;

    @OneToMany(mappedBy = "parentComment", fetch = FetchType.LAZY)
    private List<TaskComment> replies;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subtask_id", insertable = false, updatable = false)
    private Subtask subtask;
}