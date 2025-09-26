package com.spmorangle.crm.taskmanagement.service;

import java.util.List;

import com.spmorangle.crm.taskmanagement.dto.CreateTaskDto;
import com.spmorangle.crm.taskmanagement.dto.CreateTaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.TaskResponseDto;

public interface TaskService {

    CreateTaskResponseDto createTask(CreateTaskDto createTaskDto, Long currentUserId);
    
    List<TaskResponseDto> getProjectTasks(Long projectId);
    
    List<TaskResponseDto> getPersonalTasks(Long userId);
    
    List<TaskResponseDto> getAllUserTasks(Long userId);
    
    void deleteTask(Long taskId, Long currentUserId);

}
