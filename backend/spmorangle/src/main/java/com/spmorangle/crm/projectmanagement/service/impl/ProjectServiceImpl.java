package com.spmorangle.crm.projectmanagement.service.impl;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.spmorangle.common.converter.UserConverter;
import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.crm.usermanagement.dto.UserResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spmorangle.crm.projectmanagement.dto.CreateProjectDto;
import com.spmorangle.crm.projectmanagement.dto.ProjectResponseDto;
import com.spmorangle.crm.projectmanagement.model.Project;
import com.spmorangle.crm.projectmanagement.repository.ProjectRepository;
import com.spmorangle.crm.projectmanagement.service.ProjectService;
import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Override
    public List<ProjectResponseDto> getUserProjects(Long userId) {
        log.info("Getting projects for user: {}", userId);
        
        List<Project> projects = projectRepository.findUserProjects(userId);
        
        return projects.stream()
                .map(this::mapToProjectResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectResponseDto createProject(CreateProjectDto createProjectDto, Long currentUserId) {
        log.info("Creating project with name: {} for user: {}", createProjectDto.getName(), currentUserId);
        
        Project project = new Project();
        project.setName(createProjectDto.getName());
        project.setDescription(createProjectDto.getDescription());
        project.setOwnerId(currentUserId);
        project.setCreatedBy(currentUserId);
        project.setCreatedAt(OffsetDateTime.now());
        project.setUpdatedAt(OffsetDateTime.now());
        
        Project savedProject = projectRepository.save(project);
        log.info("Project created with ID: {}", savedProject.getId());
        
        return mapToProjectResponseDto(savedProject);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long projectId, Long currentUserId) {
        log.info("Deleting project: {} by user: {}", projectId, currentUserId);
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        // Only owner can delete the project
        if (!project.getOwnerId().equals(currentUserId)) {
            throw new RuntimeException("Only project owner can delete the project");
        }
        
        project.setDeleteInd(true);
        project.setUpdatedBy(currentUserId);
        project.setUpdatedAt(OffsetDateTime.now());
        
        projectRepository.save(project);
        log.info("Project {} marked as deleted", projectId);
    }

    @Override
    public List<UserResponseDto> getProjectMembers(Long projectId) {
        return userRepository.findUsersInProject(projectId).stream()
                .map(UserConverter::convert)
                .toList();
    }


    private ProjectResponseDto mapToProjectResponseDto(Project project) {
        // Get task counts for the project
        var tasks = taskRepository.findByProjectIdAndNotDeleted(project.getId());
        int taskCount = tasks.size();
        int completedTaskCount = (int) tasks.stream()
                .filter(task -> task.getStatus() == Status.COMPLETED)
                .count();

        return ProjectResponseDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerId(project.getOwnerId())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .taskCount(taskCount)
                .completedTaskCount(completedTaskCount)
                .build();
    }

    @Override
    public Long getOwnerId(Long projectId) {
        return projectRepository.findById(projectId)
                .map(Project::getOwnerId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }

    @Override
    public Map<Long, Long> getProjectOwners(Set<Long> projectIds) {
        if (projectIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return projectRepository.findAllById(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, Project::getOwnerId));
    }
}
