package com.spmorangle.crm.projectmanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spmorangle.common.model.User;
import com.spmorangle.common.service.UserContextService;
import com.spmorangle.crm.projectmanagement.dto.CreateProjectDto;
import com.spmorangle.crm.projectmanagement.dto.ProjectResponseDto;
import com.spmorangle.crm.projectmanagement.service.ProjectService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserContextService userContextService;

    @GetMapping
    public ResponseEntity<List<ProjectResponseDto>> getUserProjects(
            ) {
        User user = userContextService.getRequestingUser();
        log.info("Getting projects for user: {}", user.getId());
        List<ProjectResponseDto> projects = projectService.getUserProjects(user.getId());
        return ResponseEntity.ok(projects);
    }

    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(
            @Valid @RequestBody CreateProjectDto createProjectDto) {
        User user = userContextService.getRequestingUser();
        log.info("Creating project for user: {}", user.getId());
        ProjectResponseDto project = projectService.createProject(createProjectDto, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long projectId) {
        User user = userContextService.getRequestingUser();
        log.info("Deleting project: {} by user: {}", projectId, user.getId());
        projectService.deleteProject(projectId, user.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
