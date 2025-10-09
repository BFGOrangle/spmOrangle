package com.spmorangle.crm.projectmanagement.controller;

import java.util.List;

import com.spmorangle.crm.usermanagement.dto.UserResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProjectResponseDto> createProject(
            @Valid @RequestBody CreateProjectDto createProjectDto) {
        User user = userContextService.getRequestingUser();
        log.info("Creating project for user: {}", user.getId());
        ProjectResponseDto project = projectService.createProject(createProjectDto, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

//    @PutMapping
//    @PreAuthorize("hasRole('MANAGER')")
//    public ResponseEntity<ProjectResponseDto> updateProject(
//            @Valid @RequestBody UpdateProjectDto updateProjectDto){
//        User user = userContextService.getRequestingUser();
//        log.info("{} is updating project", user.getId());
//        ProjectResponseDto updatedProject = projectService.updateProject(updateProjectDto, user.getId());
//        return ResponseEntity.status(HttpStatus.OK).body(updatedProject);
//    }

    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long projectId) {
        User user = userContextService.getRequestingUser();
        log.info("Deleting project: {} by user: {}", projectId, user.getId());
        projectService.deleteProject(projectId, user.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<UserResponseDto>> getProjectMembers(
            @PathVariable Long projectId) {
        List<UserResponseDto> members = projectService.getProjectMembers(projectId);
        return ResponseEntity.ok(members);
    }

}
