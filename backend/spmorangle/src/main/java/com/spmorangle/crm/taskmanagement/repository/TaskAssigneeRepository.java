package com.spmorangle.crm.taskmanagement.repository;

import com.spmorangle.crm.taskmanagement.model.TaskAssignee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskAssigneeRepository extends JpaRepository<TaskAssignee, Long> {
}
