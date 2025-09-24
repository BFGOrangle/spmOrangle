package com.spmorangle.crm.taskmanagement.service;

import com.spmorangle.crm.taskmanagement.dto.CreateTaskDto;
import com.spmorangle.crm.taskmanagement.dto.CreateTaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.GetTaskResponseDto;

import java.util.List;

public interface TaskService {

    CreateTaskResponseDto createTask(CreateTaskDto createTaskDto, Long currentUserId);
    List<GetTaskResponseDto> getTasks(long userId);
//    List<GetSubTaskResponseDto> getSubTasks(long taskId);
}
