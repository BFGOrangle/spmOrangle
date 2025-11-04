package com.spmorangle.crm.projectmanagement.service.impl;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.spmorangle.common.converter.UserConverter;
import com.spmorangle.common.model.User;
import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.crm.projectmanagement.model.ProjectMember;
import com.spmorangle.crm.projectmanagement.repository.ProjectMemberRepository;
import com.spmorangle.crm.usermanagement.dto.UserResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spmorangle.crm.departmentmgmt.dto.DepartmentDto;
import com.spmorangle.crm.departmentmgmt.service.DepartmentQueryService;
import com.spmorangle.crm.departmentmgmt.service.DepartmentalVisibilityService;
import com.spmorangle.crm.projectmanagement.dto.CreateProjectDto;
import com.spmorangle.crm.projectmanagement.dto.ProjectResponseDto;
import com.spmorangle.crm.projectmanagement.model.Project;
import com.spmorangle.crm.projectmanagement.repository.ProjectRepository;
import com.spmorangle.crm.projectmanagement.service.ProjectService;
import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.model.Task;
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
    private final ProjectMemberRepository projectMemberRepository;
    private final DepartmentQueryService departmentQueryService;
    private final DepartmentalVisibilityService departmentalVisibilityService;

    @Override
    public List<ProjectResponseDto> getUserProjects(Long userId) {
        log.info("Getting projects for user: {}", userId);

        // Get the user to determine their role and department
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        



        String userRole = user.getRoleType();
        Long userDepartmentId = user.getDepartmentId();

        log.info("User role: {}, department id: {}", userRole, userDepartmentId);

        // Get projects where user is a member (owner or collaborator)
        List<Project> memberProjects = projectRepository.findUserProjects(userId);
        log.info("Found {} member projects for user {}", memberProjects.size(), userId);


        Set<Long> visibleDepartmentIds = getUserVisibleDepartmentIds(userId);
        memberProjects = memberProjects.stream()
            .filter(project -> canUserSeeProjectByDepartment(project, visibleDepartmentIds))
            .collect(Collectors.toList());

        // Initialize result list with member projects
        List<ProjectResponseDto> result = new ArrayList<>();

        // Add member projects (isRelated = false)
        for (Project project : memberProjects) {
            result.add(mapToProjectResponseDto(project, userId, false));
        }

        // For MANAGER role: Also fetch related cross-department projects
        if ("MANAGER".equalsIgnoreCase(userRole) && userDepartmentId != null) {
            log.info("Manager role detected - fetching related cross-department projects for department: {}", userDepartmentId);
            List<Project> relatedProjects = projectRepository.findProjectsWithDepartmentStaff(userId, userDepartmentId);
            log.info("Found {} related cross-department projects", relatedProjects.size());

            relatedProjects = relatedProjects.stream()
            .filter(project -> canUserSeeProjectByDepartment(project, visibleDepartmentIds))
            .collect(Collectors.toList());

            log.info("RelatedProjects: {}", relatedProjects);

            // Add related projects (isRelated = true)
            for (Project project : relatedProjects) {
                log.info("Sorting through {} of Userid {}", project, userId);
                result.add(mapToProjectResponseDto(project, userId, true));
            }
        }

        log.info("Returning total of {} projects for user {}", result.size(), userId);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectResponseDto createProject(CreateProjectDto createProjectDto, Long currentUserId) {
        log.info("Creating project with name: {} for user: {}", createProjectDto.getName(), currentUserId);

        // Create and save the project
        Project project = new Project();
        project.setName(createProjectDto.getName());
        project.setDescription(createProjectDto.getDescription());
        project.setOwnerId(currentUserId);
        project.setCreatedBy(currentUserId);
        project.setUpdatedBy(currentUserId);
        project.setCreatedAt(OffsetDateTime.now());
        project.setUpdatedAt(OffsetDateTime.now());

        Project savedProject = projectRepository.save(project);
        log.info("Project created with ID: {}", savedProject.getId());

        // Get the manager's information
        User manager = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Collect all member IDs using a Set to avoid duplicates
        Set<Long> memberIds = new HashSet<>();

        // Add manager as a member
        memberIds.add(currentUserId);

        // Add all active users from manager's department
        if (manager.getDepartmentId() != null) {
            List<User> departmentUsers = userRepository.findByDepartmentId(manager.getDepartmentId());
            departmentUsers.stream()
                    .filter(User::getIsActive) // Only active users
                    .map(User::getId)
                    .forEach(memberIds::add);
            log.info("Added {} active users from department id: {}", departmentUsers.size(), manager.getDepartmentId());
        }

        // Add additional members if specified
        if (createProjectDto.getAdditionalMemberIds() != null && !createProjectDto.getAdditionalMemberIds().isEmpty()) {
            memberIds.addAll(createProjectDto.getAdditionalMemberIds());
            log.info("Added {} additional members", createProjectDto.getAdditionalMemberIds().size());
        }

        // Create ProjectMember entries for all members
        List<ProjectMember> projectMembers = new ArrayList<>();
        for (Long memberId : memberIds) {
            ProjectMember projectMember = new ProjectMember();
            projectMember.setProjectId(savedProject.getId());
            projectMember.setUserId(memberId);
            projectMember.setAddedBy(currentUserId);
            projectMembers.add(projectMember);
        }

        // Save all project members in one batch
        if (!projectMembers.isEmpty()) {
            projectMemberRepository.saveAll(projectMembers);
            log.info("Added {} total members to project ID: {}", projectMembers.size(), savedProject.getId());
        }

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


    /**
     * Map Project entity to ProjectResponseDto with metadata
     *
     * @param project The project to map
     * @param userId The current user's ID (to calculate isOwner)
     * @param isRelated Whether this is a related cross-department project
     * @return ProjectResponseDto with all metadata fields populated
     */
    private ProjectResponseDto mapToProjectResponseDto(Project project, Long userId, boolean isRelated) {
        // Get task counts for the project
        var tasks = taskRepository.findByProjectIdAndNotDeleted(project.getId());
        int taskCount = tasks.size();
        int completedTaskCount = (int) tasks.stream()
                .filter(task -> task.getStatus() == Status.COMPLETED)
                .count();

        // Calculate isOwner flag
        boolean isOwner = project.getOwnerId().equals(userId);

        // Get department id from project owner
        String departmentName = null;
        try {
            User owner = userRepository.findById(project.getOwnerId()).orElse(null);
            if (owner != null && owner.getDepartmentId() != null) {
                departmentName = departmentQueryService.getById(owner.getDepartmentId())
                .map(DepartmentDto::getName)
                .orElse(null);
            }
        } catch (Exception e) {
            log.warn("Could not fetch owner department for project {}: {}", project.getId(), e.getMessage());
        }

        return ProjectResponseDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerId(project.getOwnerId())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .taskCount(taskCount)
                .completedTaskCount(completedTaskCount)
                .isOwner(isOwner)
                .isRelated(isRelated)
                .departmentName(departmentName)
                .build();
    }

    /**
     * Legacy mapping method for backward compatibility
     * Used by createProject which doesn't need permission metadata
     */
    private ProjectResponseDto mapToProjectResponseDto(Project project) {
        return mapToProjectResponseDto(project, project.getOwnerId(), false);
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

    @Override
    public List<ProjectResponseDto> getProjectsByIds(Set<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Collections.emptyList();
        }

        return projectRepository.findByIdIn(projectIds).stream()
                .filter(project -> !project.isDeleteInd())
                .map(this::mapToProjectResponseDto)
                .toList();
    }


    private Set<Long> getUserVisibleDepartmentIds(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // Get department of user
        Long userDepartmentId= user.getDepartmentId();
        if (userDepartmentId == null) {
            log.warn("User {} has no department assigned", userId);
            return Collections.emptySet();
        }

        // Check what other departments are visible to our user
        return departmentQueryService.getById(userDepartmentId)
            .map(deptDto -> departmentalVisibilityService.visibleDepartmentsForAssignedDept(deptDto.getId()))
            .orElse(Collections.emptySet());
    }

    private Boolean canUserSeeProjectByDepartment(Project project, Set<Long> userVisibleDepartmentIds) {
        if (userVisibleDepartmentIds.isEmpty()) {
            // Users with no department can see all their member projects
            // Department visibility checks don't apply
            return true;
        }

        List<ProjectMember> projectMembers = projectMemberRepository.findByProjectId(project.getId());
        
        List<Long> memberIds = projectMembers.stream()
            .map(ProjectMember::getUserId)
            .collect(Collectors.toList());

        memberIds.add(project.getOwnerId());


        for (Long memberId : memberIds) {
            User member = userRepository.findById(memberId).orElse(null);
            if (member == null || member.getDepartmentId() == null) {
                continue;
            }

            if (departmentalVisibilityService.canUserSeeTask(userVisibleDepartmentIds, memberId)) {
                return true;
            }
        }

        return false;
    }
}
