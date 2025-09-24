package com.spmorangle.crm.taskmanagement.repository;

import com.spmorangle.crm.taskmanagement.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> getTasksById(long id);

    List<Task> getTasksByOwnerId(long ownerId);
}
