package com.spmorangle.crm.projectmanagement.service.impl;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.spmorangle.common.converter.UserConverter;
import com.spmorangle.common.enums.UserType;
import com.spmorangle.common.model.User;
import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.crm.departmentmgmt.dto.DepartmentDto;
import com.spmorangle.crm.departmentmgmt.repository.DepartmentRepository;
import com.spmorangle.crm.departmentmgmt.service.DepartmentQueryService;
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
    private final DepartmentRepository departmentRepository;
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
            .filter(project -> project.getId() != 0L) // Exclude Personal Tasks Repository (Project ID 0)
            .filter(project -> canUserSeeProjectByDepartment(project, visibleDepartmentIds))
            .collect(Collectors.toList());

        // Initialize result list with member projects
        List<ProjectResponseDto> result = new ArrayList<>();

        // Add member projects (isRelated = false)
        log.info("👤 Processing {} member projects (projects where user is owner/member)", memberProjects.size());
        for (Project project : memberProjects) {
            log.info("  → Member Project ID: {}, Name: '{}', OwnerId: {}",
                     project.getId(), project.getName(), project.getOwnerId());
            result.add(mapToProjectResponseDto(project, userId, false));
        }

        // Fetch related cross-department projects for all users with departments
        // Related projects are projects where colleagues from visible departments are working,
        // but the user is not a direct member (view-only access)
        if (userDepartmentId != null) {
            log.info("User {} has department {} - fetching related cross-department projects for visible departments: {}",
                     userId, userDepartmentId, visibleDepartmentIds);
            List<Project> relatedProjects = projectRepository.findProjectsWithDepartmentStaff(userId, visibleDepartmentIds);
            log.info("Found {} related cross-department projects before filtering", relatedProjects.size());

            // Log the raw related projects before department filtering
            for (Project rp : relatedProjects) {
                log.info("  → Raw Related Project ID: {}, Name: '{}', OwnerId: {}",
                         rp.getId(), rp.getName(), rp.getOwnerId());
            }

            relatedProjects = relatedProjects.stream()
            .filter(project -> project.getId() != 0L) // Exclude Personal Tasks Repository (Project ID 0)
            .filter(project -> canUserSeeProjectByDepartment(project, visibleDepartmentIds))
            .collect(Collectors.toList());

            log.info("RelatedProjects: {}", relatedProjects);

            // Add related projects (isRelated = true)
            for (Project project : relatedProjects) {
                // Log which staff members from visible departments are in this project
                List<ProjectMember> members = projectMemberRepository.findByProjectId(project.getId());
                List<Long> staffInVisibleDepts = members.stream()
                        .map(ProjectMember::getUserId)
                        .filter(memberId -> {
                            User member = userRepository.findById(memberId).orElse(null);
                            return member != null && visibleDepartmentIds.contains(member.getDepartmentId());
                        })
                        .toList();

                log.info("🔗 Related project '{}' (ID: {}) has {} staff from my visible departments: {}",
                         project.getName(), project.getId(), staffInVisibleDepts.size(), staffInVisibleDepts);

                result.add(mapToProjectResponseDto(project, userId, true));
            }
        }

        log.info("Returning total of {} projects for user {}", result.size(), userId);
        log.info("📋 Project IDs in result: {}", result.stream().map(p -> p.getId()).toList());
        return result;
    }

    private boolean isDepartmentScopedRole(String role) {
        if (role == null) {
            return false;
        }

        return UserType.MANAGER.getCode().equalsIgnoreCase(role)
                || UserType.DIRECTOR.getCode().equalsIgnoreCase(role);
    }

    private Set<String> resolveDepartmentScopeNames(String departmentName) {
        if (departmentName == null || departmentName.isEmpty()) {
            return Set.of();
        }

        return departmentRepository.findByNameIgnoreCase(departmentName)
                .map(root -> departmentQueryService.getDescendants(root.getId(), true).stream()
                        .map(DepartmentDto::getName)
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(name -> !name.isEmpty())
                        .collect(Collectors.toCollection(LinkedHashSet::new)))
                .orElseGet(() -> {
                    LinkedHashSet<String> fallback = new LinkedHashSet<>();
                    fallback.add(departmentName);
                    return fallback;
                });
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

        // Collect all project owner IDs using a Set to avoid duplicates
        Set<Long> ownerIds = new HashSet<>();

        // Add creator as owner
        ownerIds.add(currentUserId);

        // Add additional members as owners if specified (from /assignable endpoint)
        if (createProjectDto.getAdditionalMemberIds() != null && !createProjectDto.getAdditionalMemberIds().isEmpty()) {
            ownerIds.addAll(createProjectDto.getAdditionalMemberIds());
            log.info("Added {} additional project owners", createProjectDto.getAdditionalMemberIds().size());
        }

        // Create ProjectMember entries for all owners
        List<ProjectMember> projectMembers = new ArrayList<>();
        for (Long ownerId : ownerIds) {
            ProjectMember projectMember = new ProjectMember();
            projectMember.setProjectId(savedProject.getId());
            projectMember.setUserId(ownerId);
            projectMember.setAddedBy(currentUserId);
            projectMember.setOwner(true); // All selected members are project owners
            log.info("Marked user {} as project owner for project {}", ownerId, savedProject.getId());

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

        // Only project owners (checked via is_owner flag) can delete the project
        if (!isUserProjectOwner(currentUserId, projectId)) {
            throw new RuntimeException("Only project owners can delete the project");
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

        // Check if user is a project member (not just owner)
        boolean isProjectMember = isUserProjectMember(userId, project.getId());

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

        // Detailed logging for project visibility
        String projectType = isOwner ? "MY PROJECT (Owner)" :
                             isProjectMember ? "MY PROJECT (Member)" :
                             "RELATED PROJECT (View-Only)";

        log.info("📂 Project '{}' (ID: {}) - Type: {}, isOwner: {}, isMember: {}, isRelated: {}, ownerId: {}, viewingUserId: {}",
                 project.getName(), project.getId(), projectType, isOwner, isProjectMember, isRelated,
                 project.getOwnerId(), userId);

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

    @Override
    public boolean isUserProjectMember(Long userId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        // Check if user is the owner
        if (project.getOwnerId().equals(userId)) {
            return true;
        }

        // Check if user is a project member
        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        return members.stream().anyMatch(m -> m.getUserId() == userId);
    }

    @Override
    public boolean isUserProjectOwner(Long userId, Long projectId) {
        return projectMemberRepository.existsByProjectIdAndUserIdAndIsOwner(projectId, userId, true);
    }

    @Override
    public List<Long> getProjectOwnerIds(Long projectId) {
        List<ProjectMember> owners = projectMemberRepository.findOwnersByProjectId(projectId);
        return owners.stream()
                .map(ProjectMember::getUserId)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addProjectOwner(Long projectId, Long userId, Long addedBy) {
        log.info("Adding project owner: userId={} to projectId={}", userId, projectId);

        // Check if user is already a project member
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);

        if (isMember) {
            // User is already a member, just update is_owner flag
            List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
            ProjectMember member = members.stream()
                    .filter(m -> m.getUserId() == userId)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Project member not found"));

            member.setOwner(true);
            projectMemberRepository.save(member);
            log.info("Updated existing member to owner");
        } else {
            // User is not a member, add them as owner
            ProjectMember newOwner = new ProjectMember();
            newOwner.setProjectId(projectId);
            newOwner.setUserId(userId);
            newOwner.setOwner(true);
            newOwner.setAddedBy(addedBy);
            projectMemberRepository.save(newOwner);
            log.info("Added new member as owner");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeProjectOwner(Long projectId, Long userId) {
        log.info("Removing project owner: userId={} from projectId={}", userId, projectId);

        // Check if there are multiple owners
        List<ProjectMember> owners = projectMemberRepository.findOwnersByProjectId(projectId);

        if (owners.size() <= 1) {
            throw new RuntimeException("Cannot remove the last owner from a project");
        }

        // Find the member and update is_owner flag
        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        ProjectMember member = members.stream()
                .filter(m -> m.getUserId() == userId && m.isOwner())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User is not a project owner"));

        member.setOwner(false);
        projectMemberRepository.save(member);
        log.info("Removed owner status from user {}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addProjectMember(Long projectId, Long userId, Long addedBy) {
        log.info("Adding project member: userId={} to projectId={}", userId, projectId);

        // Check if user is already a project member
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);

        if (isMember) {
            log.info("User {} is already a member of project {}", userId, projectId);
            return; // No-op if already a member
        }

        // Add user as a regular project member (not owner)
        ProjectMember newMember = new ProjectMember();
        newMember.setProjectId(projectId);
        newMember.setUserId(userId);
        newMember.setOwner(false); // Regular member, not owner
        newMember.setAddedBy(addedBy);
        projectMemberRepository.save(newMember);
        log.info("Added user {} as project member (non-owner) to project {}", userId, projectId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeProjectMember(Long projectId, Long userId) {
        log.info("Removing project member: userId={} from projectId={}", userId, projectId);

        // Find the member
        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        ProjectMember member = members.stream()
                .filter(m -> m.getUserId() == userId)
                .findFirst()
                .orElse(null);

        if (member == null) {
            log.info("User {} is not a member of project {}, nothing to remove", userId, projectId);
            return; // No-op if not a member
        }

        // Don't remove if user is an owner
        if (member.isOwner()) {
            log.info("User {} is a project owner, not removing from project {}", userId, projectId);
            return; // Don't remove owners
        }

        // Remove the project member
        projectMemberRepository.delete(member);
        log.info("Removed user {} from project {}", userId, projectId);
    }
}
