package com.spmorangle.crm.taskmanagement.service;

import java.util.List;

import com.spmorangle.crm.taskmanagement.dto.CreateTaskDto;
import com.spmorangle.crm.taskmanagement.dto.CreateTaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.TaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.UpdateTaskDto;
import com.spmorangle.crm.taskmanagement.dto.UpdateTaskResponseDto;
import com.spmorangle.crm.taskmanagement.model.Task;

public interface TaskService {

    /**
     * create task with current user as owner
     */
    CreateTaskResponseDto createTask(CreateTaskDto createTaskDto, Long currentUserId);

    /**
     * create task with specified ownerId
     */
    CreateTaskResponseDto createTask(CreateTaskDto createTaskDto, Long taskOwnerId, Long currentUserId);

    List<TaskResponseDto> getProjectTasks(Long userId, Long projectId);

    TaskResponseDto getTaskById(Long taskId, Long currentUserId);
    
    List<TaskResponseDto> getPersonalTasks(Long userId);
    
    List<TaskResponseDto> getAllUserTasks(Long userId);

    UpdateTaskResponseDto updateTask(UpdateTaskDto updateTaskDto, Long currentUserId);

    void deleteTask(Long taskId, Long currentUserId);

    boolean canUserUpdateTask(Long taskId, Long userId);

    boolean canUserDeleteTask(Long taskId, Long userId);

}
