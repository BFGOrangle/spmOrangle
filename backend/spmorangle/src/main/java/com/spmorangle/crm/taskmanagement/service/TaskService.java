package com.spmorangle.crm.taskmanagement.service;

import java.time.OffsetDateTime;
import java.util.List;

import com.spmorangle.crm.taskmanagement.dto.CreateTaskDto;
import com.spmorangle.crm.taskmanagement.dto.CreateTaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.TaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.UpdateTaskDto;
import com.spmorangle.crm.taskmanagement.dto.UpdateTaskResponseDto;
import com.spmorangle.crm.taskmanagement.enums.CalendarView;

public interface TaskService {

    /**
     * create task with current user as owner
     */
    CreateTaskResponseDto createTask(CreateTaskDto createTaskDto, Long currentUserId);

    /**
     * create task with specified ownerId
     */
    CreateTaskResponseDto createTask(CreateTaskDto createTaskDto, Long taskOwnerId, Long currentUserId);

    TaskResponseDto getTaskById(Long taskId, Long currentUserId);    

    List<TaskResponseDto> getRelatedTasks(Long userId);

    UpdateTaskResponseDto updateTask(UpdateTaskDto updateTaskDto, Long currentUserId);

    void deleteTask(Long taskId, Long currentUserId);

    boolean canUserUpdateTask(Long taskId, Long userId);

    boolean canUserDeleteTask(Long taskId, Long userId);

    List<TaskResponseDto> getUserTasksDueTmr(Long userId, OffsetDateTime startOfDay, OffsetDateTime endOfDay);

    List<TaskResponseDto> getUserTasksDueTomorrowForDigest(Long userId, OffsetDateTime startOfDay, OffsetDateTime endOfDay);

    // kanban view methods
    List<TaskResponseDto> getProjectTasks(Long userId, Long projectId);
    List<TaskResponseDto> getPersonalTasks(Long userId);
    List<TaskResponseDto> getAllUserTasks(Long userId);

    // calendar view methods
    List<TaskResponseDto> getProjectTasksForCalendar(Long userId, Long projectId, CalendarView calendarView, OffsetDateTime referenceDate);
    List<TaskResponseDto> getPersonalTasksForCalendar(Long userId, CalendarView calendarView, OffsetDateTime referenceDate);
    List<TaskResponseDto> getAllUserTasksForCalendar(Long userId, CalendarView calendarView, OffsetDateTime referenceDate);


}
