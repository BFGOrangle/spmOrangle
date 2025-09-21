package com.spmorangle.crm.taskmanagement.repository;

import com.spmorangle.crm.taskmanagement.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

}
