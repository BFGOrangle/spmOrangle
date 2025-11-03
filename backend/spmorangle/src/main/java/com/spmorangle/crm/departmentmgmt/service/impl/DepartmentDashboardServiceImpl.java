package com.spmorangle.crm.departmentmgmt.service.impl;

import com.spmorangle.common.enums.UserType;
import com.spmorangle.common.model.User;
import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.crm.departmentmgmt.dto.DashboardMetricsDto;
import com.spmorangle.crm.departmentmgmt.dto.DepartmentDashboardResponseDto;
import com.spmorangle.crm.departmentmgmt.dto.ProjectHealthCardDto;
import com.spmorangle.crm.departmentmgmt.dto.TaskDashboardItemDto;
import com.spmorangle.crm.departmentmgmt.dto.TeamLoadEntryDto;
import com.spmorangle.crm.departmentmgmt.model.Department;
import com.spmorangle.crm.departmentmgmt.repository.DepartmentRepository;
import com.spmorangle.crm.departmentmgmt.service.DepartmentDashboardService;
import com.spmorangle.crm.departmentmgmt.service.DepartmentQueryService;
import com.spmorangle.crm.projectmanagement.model.Project;
import com.spmorangle.crm.projectmanagement.repository.ProjectRepository;
import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.model.TaskAssignee;
import com.spmorangle.crm.taskmanagement.repository.TaskAssigneeRepository;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentDashboardServiceImpl implements DepartmentDashboardService {

    private static final Set<String> SUPPORTED_ROLES = Set.of(
            UserType.MANAGER.getCode(),
            UserType.DIRECTOR.getCode(),
            UserType.HR.getCode()
    );

    private static final int UPCOMING_WINDOW_DAYS = 14;

    private final DepartmentRepository departmentRepository;
    private final DepartmentQueryService departmentQueryService;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final ProjectRepository projectRepository;

    @Override
    public DepartmentDashboardResponseDto getDepartmentDashboard(User user) {
        validateUserRole(user);

        String departmentName = Optional.ofNullable(user.getDepartment())
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .orElse(null);

        if (departmentName == null) {
            log.warn("User {} has no department assigned; returning empty dashboard", user.getId());
            return buildEmptyResponse(null, List.of());
        }

        Set<String> visibleDepartmentNames = resolveVisibleDepartmentNames(departmentName);
        if (visibleDepartmentNames.isEmpty()) {
            log.warn("No visible departments resolved for user {} and department {}", user.getId(), departmentName);
            return buildEmptyResponse(departmentName, List.of());
        }

        List<String> orderedDepartments = visibleDepartmentNames.stream().toList();

        // Fetch active members within the visible departments
        Set<String> lowerCaseDepartments = visibleDepartmentNames.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        List<User> departmentMembers = userRepository.findActiveUsersByDepartmentsIgnoreCase(lowerCaseDepartments);
        if (departmentMembers.isEmpty()) {
            log.info("No active members found for departments {}", visibleDepartmentNames);
            return buildEmptyResponse(departmentName, orderedDepartments);
        }

        Set<Long> memberIds = departmentMembers.stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        List<Task> tasks = taskRepository.findVisibleTasksForUsers(memberIds);
        if (tasks.isEmpty()) {
            log.info("No tasks found for visible departments {}", visibleDepartmentNames);
            return buildEmptyResponseWithTeam(departmentName, orderedDepartments, departmentMembers);
        }

        Set<Long> taskIds = tasks.stream()
                .map(Task::getId)
                .collect(Collectors.toSet());

        Map<Long, List<Long>> assigneeIdsByTask = fetchAssigneesGroupedByTask(taskIds);

        Set<Long> ownerIds = tasks.stream().map(Task::getOwnerId).collect(Collectors.toSet());
        Set<Long> additionalUserIds = new HashSet<>(ownerIds);
        assigneeIdsByTask.values().forEach(additionalUserIds::addAll);

        Map<Long, User> usersById = additionalUserIds.isEmpty()
                ? Map.of()
                : userRepository.findByIdIn(new ArrayList<>(additionalUserIds)).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        Map<Long, Project> projectsById = resolveProjects(tasks);

        List<TaskDashboardItemDto> taskItems = tasks.stream()
                .map(task -> mapToTaskItem(task, usersById, projectsById, assigneeIdsByTask))
                .toList();

        List<ProjectHealthCardDto> projectCards = buildProjectCards(tasks, projectsById);
        List<TaskDashboardItemDto> upcomingCommitments = selectUpcomingCommitments(taskItems);
        List<TaskDashboardItemDto> priorityQueue = selectPriorityQueue(taskItems);
        List<TeamLoadEntryDto> teamLoad = buildTeamLoad(tasks, assigneeIdsByTask, usersById, memberIds);

        DashboardMetricsDto metrics = buildMetrics(tasks, projectCards.size(), priorityQueue.size());

        return DepartmentDashboardResponseDto.builder()
                .department(departmentName)
                .includedDepartments(orderedDepartments)
                .metrics(metrics)
                .projects(projectCards)
                .upcomingCommitments(upcomingCommitments)
                .priorityQueue(priorityQueue)
                .teamLoad(teamLoad)
                .build();
    }

    private void validateUserRole(User user) {
        if (user == null) {
            throw new AccessDeniedException("User context is required");
        }
        String role = Optional.ofNullable(user.getRoleType()).orElse("");
        if (!SUPPORTED_ROLES.contains(role)) {
            throw new AccessDeniedException("Department dashboard is only available to managerial roles");
        }
    }

    private Set<String> resolveVisibleDepartmentNames(String departmentName) {
        Optional<Department> departmentOpt = departmentRepository.findByNameIgnoreCase(departmentName);
        if (departmentOpt.isEmpty()) {
            return Set.of(departmentName);
        }

        Department department = departmentOpt.get();
        return departmentQueryService.getDescendants(department.getId(), true).stream()
                .map(descendant -> descendant.getName().trim())
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Map<Long, List<Long>> fetchAssigneesGroupedByTask(Collection<Long> taskIds) {
        if (taskIds.isEmpty()) {
            return Map.of();
        }

        return taskAssigneeRepository.findByTaskIdIn(taskIds).stream()
                .collect(Collectors.groupingBy(TaskAssignee::getTaskId,
                        Collectors.mapping(TaskAssignee::getUserId, Collectors.toList())));
    }

    private Map<Long, Project> resolveProjects(List<Task> tasks) {
        Set<Long> projectIds = tasks.stream()
                .map(Task::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (projectIds.isEmpty()) {
            return Map.of();
        }

        return projectRepository.findAllById(projectIds).stream()
                .filter(project -> !project.isDeleteInd())
                .collect(Collectors.toMap(Project::getId, Function.identity()));
    }

    private TaskDashboardItemDto mapToTaskItem(Task task,
                                               Map<Long, User> usersById,
                                               Map<Long, Project> projectsById,
                                               Map<Long, List<Long>> assigneeIdsByTask) {
        User owner = usersById.get(task.getOwnerId());
        Project project = task.getProjectId() != null ? projectsById.get(task.getProjectId()) : null;

        List<Long> assignees = assigneeIdsByTask.getOrDefault(task.getId(), List.of());

        return TaskDashboardItemDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .status(task.getStatus().name())
                .taskType(task.getTaskType() != null ? task.getTaskType().name() : null)
                .priority(task.getPriority())
                .ownerId(task.getOwnerId())
                .ownerName(owner != null ? owner.getUserName() : "User " + task.getOwnerId())
                .ownerDepartment(owner != null ? owner.getDepartment() : null)
                .projectId(task.getProjectId())
                .projectName(project != null ? project.getName() : null)
                .dueDateTime(task.getDueDateTime())
                .updatedAt(task.getUpdatedAt())
                .createdAt(task.getCreatedAt())
                .assigneeIds(assignees)
                .build();
    }

    private DashboardMetricsDto buildMetrics(List<Task> tasks,
                                             int activeProjectCount,
                                             int priorityQueueCount) {
        int totalTasks = tasks.size();
        int completedTasks = (int) tasks.stream().filter(task -> task.getStatus() == Status.COMPLETED).count();
        int blockedTasks = (int) tasks.stream().filter(task -> task.getStatus() == Status.BLOCKED).count();
        int highPriorityTasks = priorityQueueCount;

        double completionRate = totalTasks == 0
                ? 0
                : ((double) completedTasks / totalTasks) * 100.0;

        return DashboardMetricsDto.builder()
                .activeProjects(activeProjectCount)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .blockedTasks(blockedTasks)
                .highPriorityTasks(highPriorityTasks)
                .completionRate(Math.round(completionRate * 10.0) / 10.0)
                .build();
    }

    private List<ProjectHealthCardDto> buildProjectCards(List<Task> tasks, Map<Long, Project> projectsById) {
        Map<Long, List<Task>> tasksByProject = tasks.stream()
                .filter(task -> task.getProjectId() != null)
                .collect(Collectors.groupingBy(Task::getProjectId));

        return tasksByProject.entrySet().stream()
                .map(entry -> {
                    Long projectId = entry.getKey();
                    List<Task> projectTasks = entry.getValue();
                    int totalTasks = projectTasks.size();
                    int completedTasks = (int) projectTasks.stream().filter(task -> task.getStatus() == Status.COMPLETED).count();
                    int blockedTasks = (int) projectTasks.stream().filter(task -> task.getStatus() == Status.BLOCKED).count();

                    String status;
                    if (totalTasks > 0 && completedTasks == totalTasks) {
                        status = "Completed";
                    } else if (blockedTasks > 0) {
                        status = "At Risk";
                    } else {
                        status = "Active";
                    }

                    int completionPercentage = totalTasks == 0 ? 0
                            : (int) Math.round(((double) completedTasks / totalTasks) * 100.0);

                    String projectName = Optional.ofNullable(projectsById.get(projectId))
                            .map(Project::getName)
                            .orElse("Project " + projectId);

                    return ProjectHealthCardDto.builder()
                            .projectId(projectId)
                            .projectName(projectName)
                            .status(status)
                            .completionPercentage(completionPercentage)
                            .totalTasks(totalTasks)
                            .completedTasks(completedTasks)
                            .blockedTasks(blockedTasks)
                            .build();
                })
                .sorted(Comparator.comparing(ProjectHealthCardDto::getProjectName))
                .toList();
    }

    private List<TaskDashboardItemDto> selectUpcomingCommitments(List<TaskDashboardItemDto> taskItems) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime horizon = now.plusDays(UPCOMING_WINDOW_DAYS);

        return taskItems.stream()
                .filter(item -> item.getDueDateTime() != null)
                .filter(item -> !"COMPLETED".equals(item.getStatus()))
                .filter(item -> !item.getDueDateTime().isBefore(now))
                .filter(item -> !item.getDueDateTime().isAfter(horizon))
                .sorted(Comparator.comparing(TaskDashboardItemDto::getDueDateTime))
                .toList();
    }

    private List<TaskDashboardItemDto> selectPriorityQueue(List<TaskDashboardItemDto> taskItems) {
        return taskItems.stream()
                .filter(item -> !"COMPLETED".equals(item.getStatus()))
                .filter(item -> "BLOCKED".equals(item.getStatus())
                        || (item.getPriority() != null && item.getPriority() >= 8)
                        || "BUG".equals(item.getTaskType()))
                .sorted(Comparator
                        .comparing((TaskDashboardItemDto item) -> item.getPriority() == null ? 0 : item.getPriority()).reversed()
                        .thenComparing(TaskDashboardItemDto::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(TaskDashboardItemDto::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private List<TeamLoadEntryDto> buildTeamLoad(List<Task> tasks,
                                                 Map<Long, List<Long>> assigneeIdsByTask,
                                                 Map<Long, User> usersById,
                                                 Set<Long> visibleMemberIds) {
        Map<Long, Set<Long>> taskIdsByUser = new HashMap<>();
        Map<Long, Integer> blockedCounts = new HashMap<>();

        for (Task task : tasks) {
            boolean isBlocked = task.getStatus() == Status.BLOCKED;

            if (visibleMemberIds.contains(task.getOwnerId())) {
                taskIdsByUser.computeIfAbsent(task.getOwnerId(), key -> new HashSet<>()).add(task.getId());
                if (isBlocked) {
                    blockedCounts.merge(task.getOwnerId(), 1, Integer::sum);
                }
            }

            for (Long assigneeId : assigneeIdsByTask.getOrDefault(task.getId(), List.of())) {
                if (!visibleMemberIds.contains(assigneeId)) {
                    continue;
                }
                taskIdsByUser.computeIfAbsent(assigneeId, key -> new HashSet<>()).add(task.getId());
                if (isBlocked) {
                    blockedCounts.merge(assigneeId, 1, Integer::sum);
                }
            }
        }

        return taskIdsByUser.entrySet().stream()
                .map(entry -> {
                    Long userId = entry.getKey();
                    Set<Long> userTaskIds = entry.getValue();
                    User member = usersById.get(userId);
                    String fullName = member != null ? member.getUserName() : "User " + userId;
                    String department = member != null ? member.getDepartment() : null;
                    int blockedCount = blockedCounts.getOrDefault(userId, 0);

                    return TeamLoadEntryDto.builder()
                            .userId(userId)
                            .fullName(fullName)
                            .department(department)
                            .taskCount(userTaskIds.size())
                            .blockedTaskCount(blockedCount)
                            .build();
                })
                .sorted(Comparator
                        .comparing(TeamLoadEntryDto::getTaskCount).reversed()
                        .thenComparing(TeamLoadEntryDto::getFullName))
                .toList();
    }

    private DepartmentDashboardResponseDto buildEmptyResponse(String departmentName, List<String> includedDepartments) {
        return DepartmentDashboardResponseDto.builder()
                .department(departmentName)
                .includedDepartments(includedDepartments)
                .metrics(DashboardMetricsDto.builder()
                        .activeProjects(0)
                        .totalTasks(0)
                        .completedTasks(0)
                        .blockedTasks(0)
                        .highPriorityTasks(0)
                        .completionRate(0)
                        .build())
                .projects(List.of())
                .upcomingCommitments(List.of())
                .priorityQueue(List.of())
                .teamLoad(List.of())
                .build();
    }

    private DepartmentDashboardResponseDto buildEmptyResponseWithTeam(String departmentName,
                                                                      List<String> includedDepartments,
                                                                      List<User> departmentMembers) {
        List<TeamLoadEntryDto> teamLoad = departmentMembers.stream()
                .map(member -> TeamLoadEntryDto.builder()
                        .userId(member.getId())
                        .fullName(member.getUserName())
                        .department(member.getDepartment())
                        .taskCount(0)
                        .blockedTaskCount(0)
                        .build())
                .sorted(Comparator.comparing(TeamLoadEntryDto::getFullName))
                .toList();

        return DepartmentDashboardResponseDto.builder()
                .department(departmentName)
                .includedDepartments(includedDepartments)
                .metrics(DashboardMetricsDto.builder()
                        .activeProjects(0)
                        .totalTasks(0)
                        .completedTasks(0)
                        .blockedTasks(0)
                        .highPriorityTasks(0)
                        .completionRate(0)
                        .build())
                .projects(List.of())
                .upcomingCommitments(List.of())
                .priorityQueue(List.of())
                .teamLoad(teamLoad)
                .build();
    }
}
