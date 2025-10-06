package com.spmorangle.crm.taskmanagement.repository;

import com.spmorangle.crm.taskmanagement.model.TaskAssignee;
import com.spmorangle.crm.taskmanagement.model.TaskAssigneeCK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskAssigneeRepository extends JpaRepository<TaskAssignee, TaskAssigneeCK> {

    boolean existsByTaskIdAndUserIdAndAssignedId(long taskId, long userId, long assignedId);

    boolean existsByTaskIdAndUserId(Long taskId, Long userId);

    @Query(nativeQuery = true, value = "SELECT task_id FROM syncup.task_assignees WHERE user_id = :userId")
    List<Long> findTaskIdsUserIsAssigneeFor(Long userId);

    @Query(nativeQuery = true, value = "SELECT user_id FROM syncup.task_assignees WHERE task_id = :taskId")
    List<Long> findAssigneeIdsByTaskId(Long taskId);
}
