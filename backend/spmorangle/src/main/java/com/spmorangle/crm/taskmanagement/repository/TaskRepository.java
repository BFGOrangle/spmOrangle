package com.spmorangle.crm.taskmanagement.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spmorangle.crm.taskmanagement.model.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    @Query("SELECT t FROM Task t WHERE t.deleteInd = false AND t.projectId = :projectId")
    List<Task> findByProjectIdAndNotDeleted(@Param("projectId") Long projectId);
    
    @Query("SELECT t FROM Task t WHERE t.deleteInd = false AND (t.projectId IS NULL OR t.projectId = 0) AND t.ownerId = :ownerId")
    List<Task> findPersonalTasksByOwnerIdAndNotDeleted(@Param("ownerId") Long ownerId);
    
    @Query("SELECT t FROM Task t WHERE t.deleteInd = false AND t.ownerId = :ownerId")
    List<Task> findByOwnerIdAndNotDeleted(@Param("ownerId") Long ownerId);
    
    @Query("SELECT t FROM Task t " +
           "LEFT JOIN TaskAssignee ta ON t.id = ta.taskId " +
           "WHERE t.deleteInd = false AND (t.ownerId = :userId OR ta.userId = :userId)")
    List<Task> findUserTasks(@Param("userId") Long userId);
    
    List<Task> getTasksById(long id);

    Task getTaskById(long id);

    List<Task> getTasksByOwnerId(long ownerId);

    @Query("SELECT t FROM Task t WHERE t.deleteInd = false AND t.id = :id")
    Task findByIdAndNotDeleted(@Param("id") Long id);

    // Query for overdue task checking - returns non-deleted tasks with due date before threshold
    @Query("SELECT t FROM Task t WHERE t.deleteInd = false AND t.dueDateTime < :threshold")
    List<Task> findByDueDateTimeBefore(@Param("threshold") OffsetDateTime threshold);
}
