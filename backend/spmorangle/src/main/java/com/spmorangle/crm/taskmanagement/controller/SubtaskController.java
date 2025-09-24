package com.spmorangle.crm.taskmanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spmorangle.crm.taskmanagement.dto.CreateSubtaskDto;
import com.spmorangle.crm.taskmanagement.dto.SubtaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.UpdateSubtaskDto;
import com.spmorangle.crm.taskmanagement.service.SubtaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/subtasks")
@RequiredArgsConstructor
public class SubtaskController {

    private final SubtaskService subtaskService;

    @PostMapping
    public ResponseEntity<SubtaskResponseDto> createSubtask(
            @Valid @RequestBody CreateSubtaskDto createSubtaskDto,
            @RequestParam Long currentUserId) {
        
        log.info("Creating subtask for task {} by user {}", createSubtaskDto.getTaskId(), currentUserId);
        
        SubtaskResponseDto response = subtaskService.createSubtask(createSubtaskDto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{subtaskId}")
    public ResponseEntity<SubtaskResponseDto> getSubtask(
            @PathVariable Long subtaskId) {
        log.info("Getting subtask: {}", subtaskId);
        SubtaskResponseDto subtask = subtaskService.getSubtaskById(subtaskId);
        return ResponseEntity.ok(subtask);
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<SubtaskResponseDto>> getSubtasksByTask(
            @PathVariable Long taskId) {
        log.info("Getting subtasks for task: {}", taskId);
        List<SubtaskResponseDto> subtasks = subtaskService.getSubtasksByTaskId(taskId);
        return ResponseEntity.ok(subtasks);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<SubtaskResponseDto>> getSubtasksByProject(
            @PathVariable Long projectId) {
        log.info("Getting subtasks for project: {}", projectId);
        List<SubtaskResponseDto> subtasks = subtaskService.getSubtasksByProjectId(projectId);
        return ResponseEntity.ok(subtasks);
    }

    @PutMapping("/{subtaskId}")
    public ResponseEntity<SubtaskResponseDto> updateSubtask(
            @PathVariable Long subtaskId,
            @Valid @RequestBody UpdateSubtaskDto updateSubtaskDto,
            @RequestParam Long currentUserId) {
        
        log.info("Updating subtask: {} by user: {}", subtaskId, currentUserId);
        
        SubtaskResponseDto response = subtaskService.updateSubtask(subtaskId, updateSubtaskDto, currentUserId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{subtaskId}")
    public ResponseEntity<Void> deleteSubtask(
            @PathVariable Long subtaskId,
            @RequestParam Long currentUserId) {
        log.info("Deleting subtask: {} by user: {}", subtaskId, currentUserId);
        subtaskService.deleteSubtask(subtaskId, currentUserId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
