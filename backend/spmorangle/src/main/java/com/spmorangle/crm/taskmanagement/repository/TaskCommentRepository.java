package com.spmorangle.crm.taskmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spmorangle.crm.taskmanagement.model.TaskComment;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {

    @Query("SELECT tc FROM TaskComment tc WHERE tc.taskId = :taskId AND tc.isDeleted = false ORDER BY tc.createdAt ASC")
    List<TaskComment> findByTaskIdAndNotDeleted(@Param("taskId") Long taskId);

    @Query("SELECT tc FROM TaskComment tc WHERE tc.subtaskId = :subtaskId AND tc.isDeleted = false ORDER BY tc.createdAt ASC")
    List<TaskComment> findBySubtaskIdAndNotDeleted(@Param("subtaskId") Long subtaskId);

    @Query("SELECT tc FROM TaskComment tc WHERE tc.parentCommentId = :parentCommentId AND tc.isDeleted = false ORDER BY tc.createdAt ASC")
    List<TaskComment> findRepliesByParentCommentId(@Param("parentCommentId") Long parentCommentId);

    @Query("SELECT tc FROM TaskComment tc WHERE tc.taskId = :taskId AND tc.parentCommentId IS NULL AND tc.isDeleted = false ORDER BY tc.createdAt ASC")
    List<TaskComment> findTopLevelCommentsByTaskId(@Param("taskId") Long taskId);

    @Query("SELECT tc FROM TaskComment tc WHERE tc.subtaskId = :subtaskId AND tc.parentCommentId IS NULL AND tc.isDeleted = false ORDER BY tc.createdAt ASC")
    List<TaskComment> findTopLevelCommentsBySubtaskId(@Param("subtaskId") Long subtaskId);

    @Query("SELECT tc FROM TaskComment tc WHERE tc.projectId = :projectId AND tc.isDeleted = false ORDER BY tc.createdAt DESC")
    List<TaskComment> findByProjectIdAndNotDeleted(@Param("projectId") Long projectId);

    @Query("SELECT tc FROM TaskComment tc WHERE tc.createdBy = :userId AND tc.isDeleted = false ORDER BY tc.createdAt DESC")
    List<TaskComment> findByAuthorIdAndNotDeleted(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM syncup.task_comments tc WHERE :userId = ANY(tc.mentioned_user_ids) AND tc.is_deleted = false ORDER BY tc.created_at DESC", nativeQuery = true)
    List<TaskComment> findByMentionedUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(tc) FROM TaskComment tc WHERE tc.parentCommentId = :parentCommentId AND tc.isDeleted = false")
    long countRepliesByParentCommentId(@Param("parentCommentId") Long parentCommentId);

    @Query("SELECT tc FROM TaskComment tc WHERE tc.taskId = :taskId AND tc.isDeleted = false " +
           "AND (:authorId IS NULL OR tc.createdBy = :authorId) " +
           "AND (:isResolved IS NULL OR tc.isResolved = :isResolved) " +
           "ORDER BY tc.createdAt ASC")
    List<TaskComment> findTaskCommentsWithFilters(
        @Param("taskId") Long taskId,
        @Param("authorId") Long authorId,
        @Param("isResolved") Boolean isResolved
    );

    @Query("SELECT tc FROM TaskComment tc WHERE tc.subtaskId = :subtaskId AND tc.isDeleted = false " +
           "AND (:authorId IS NULL OR tc.createdBy = :authorId) " +
           "AND (:isResolved IS NULL OR tc.isResolved = :isResolved) " +
           "ORDER BY tc.createdAt ASC")
    List<TaskComment> findSubtaskCommentsWithFilters(
        @Param("subtaskId") Long subtaskId,
        @Param("authorId") Long authorId,
        @Param("isResolved") Boolean isResolved
    );

    @Query("SELECT DISTINCT tc.createdBy FROM TaskComment tc WHERE tc.taskId = :taskId AND tc.isDeleted = false")
    List<Long> findCommentAuthorsByTaskId(@Param("taskId") Long taskId);

    @Query("SELECT DISTINCT tc.createdBy FROM TaskComment tc WHERE tc.subtaskId = :subtaskId AND tc.isDeleted = false")
    List<Long> findCommentAuthorsBySubtaskId(@Param("subtaskId") Long subtaskId);
}