package com.spmorangle.crm.taskmanagement.service;

import com.spmorangle.crm.taskmanagement.dto.CreateTaskDto;
import com.spmorangle.crm.taskmanagement.dto.CreateTaskResponseDto;
public interface TaskService {

    CreateTaskResponseDto createTask(CreateTaskDto createTaskDto, Long currentUserId);
}
