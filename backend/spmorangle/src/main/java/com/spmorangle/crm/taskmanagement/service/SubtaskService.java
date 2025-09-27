package com.spmorangle.crm.taskmanagement.service;

import java.util.List;

import com.spmorangle.crm.taskmanagement.dto.CreateSubtaskDto;
import com.spmorangle.crm.taskmanagement.dto.SubtaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.UpdateSubtaskDto;

public interface SubtaskService {

    SubtaskResponseDto createSubtask(CreateSubtaskDto createSubtaskDto, Long currentUserId);
    
    List<SubtaskResponseDto> getSubtasksByTaskId(Long taskId);
    
    List<SubtaskResponseDto> getSubtasksByProjectId(Long projectId);
    
    SubtaskResponseDto updateSubtask(Long subtaskId, UpdateSubtaskDto updateSubtaskDto, Long currentUserId);
    
    void deleteSubtask(Long subtaskId, Long currentUserId);
    
    SubtaskResponseDto getSubtaskById(Long subtaskId);
}
