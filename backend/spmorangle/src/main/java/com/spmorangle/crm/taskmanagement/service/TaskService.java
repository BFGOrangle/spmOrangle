package com.spmorangle.crm.taskmanagement.service;

import java.util.List;

import com.spmorangle.crm.taskmanagement.dto.CreateTaskDto;
import com.spmorangle.crm.taskmanagement.dto.CreateTaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.TaskResponseDto;

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
    
    List<TaskResponseDto> getPersonalTasks(Long userId);
    
    List<TaskResponseDto> getAllUserTasks(Long userId);
    
    void deleteTask(Long taskId, Long currentUserId);

    boolean canUserUpdateOrDeleteTask(Long taskId, Long userId);

}
