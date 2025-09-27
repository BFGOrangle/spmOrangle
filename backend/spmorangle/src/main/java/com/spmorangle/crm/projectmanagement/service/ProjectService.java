package com.spmorangle.crm.projectmanagement.service;

import java.util.List;

import com.spmorangle.crm.projectmanagement.dto.CreateProjectDto;
import com.spmorangle.crm.projectmanagement.dto.ProjectResponseDto;

public interface ProjectService {
    List<ProjectResponseDto> getUserProjects(Long userId);
    ProjectResponseDto createProject(CreateProjectDto createProjectDto, Long currentUserId);
    void deleteProject(Long projectId, Long currentUserId);
}
