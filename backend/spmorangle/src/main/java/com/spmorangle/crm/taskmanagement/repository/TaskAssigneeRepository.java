package com.spmorangle.crm.taskmanagement.repository;

import com.spmorangle.crm.taskmanagement.model.TaskAssignee;
import com.spmorangle.crm.taskmanagement.model.TaskAssigneeCK;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskAssigneeRepository extends JpaRepository<TaskAssignee, TaskAssigneeCK> {

    boolean existsByTaskIdAndUserIdAndAssignedId(long taskId, long userId, long assignedId);
}
