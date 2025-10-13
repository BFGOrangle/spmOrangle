package com.spmorangle.crm.projectmanagement.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.spmorangle.crm.projectmanagement.dto.CreateProjectDto;
import com.spmorangle.crm.projectmanagement.dto.ProjectResponseDto;
import com.spmorangle.crm.usermanagement.dto.UserResponseDto;

public interface ProjectService {
    List<ProjectResponseDto> getUserProjects(Long userId);
    ProjectResponseDto createProject(CreateProjectDto createProjectDto, Long currentUserId);
    void deleteProject(Long projectId, Long currentUserId);
    List<UserResponseDto> getProjectMembers(Long projectId);
    Long getOwnerId(Long projectId);
    Map<Long, Long> getProjectOwners(Set<Long> projectIds);
    List<ProjectResponseDto> getProjectsByIds(Set<Long> projectIds);
}
