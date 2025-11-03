package com.spmorangle.crm.departmentmgmt.service.impl;

import com.spmorangle.common.enums.UserType;
import com.spmorangle.common.model.User;
import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.crm.departmentmgmt.dto.DepartmentDto;
import com.spmorangle.crm.departmentmgmt.model.Department;
import com.spmorangle.crm.departmentmgmt.repository.DepartmentRepository;
import com.spmorangle.crm.departmentmgmt.service.DepartmentQueryService;
import com.spmorangle.crm.projectmanagement.model.Project;
import com.spmorangle.crm.projectmanagement.repository.ProjectRepository;
import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.enums.TaskType;
import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.model.TaskAssignee;
import com.spmorangle.crm.taskmanagement.repository.TaskAssigneeRepository;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentDashboardServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private DepartmentQueryService departmentQueryService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskAssigneeRepository taskAssigneeRepository;
    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private DepartmentDashboardServiceImpl service;

    private User managerUser;

    @BeforeEach
    void setUp() {
        managerUser = new User();
        managerUser.setId(4L);
        managerUser.setUserName("manager_jitt");
        managerUser.setRoleType(UserType.MANAGER.getCode());
        managerUser.setDepartment("Marketing");
        managerUser.setIsActive(true);
    }

    @Test
    void getDepartmentDashboard_aggregatesMetricsWithinDepartmentScope() {
        Department marketing = new Department();
        marketing.setId(10L);
        marketing.setName("Marketing");
        marketing.setParentId(null);

        when(departmentRepository.findByNameIgnoreCase("Marketing"))
                .thenReturn(Optional.of(marketing));

        when(departmentQueryService.getDescendants(10L, true)).thenReturn(List.of(
                DepartmentDto.builder().id(10L).name("Marketing").parentId(null).build(),
                DepartmentDto.builder().id(11L).name("Marketing Ops").parentId(10L).build()
        ));

        User staffA = createStaffUser(8L, "staff_jordan", "Marketing");
        User staffB = createStaffUser(12L, "staff_yc", "Marketing");

        when(userRepository.findActiveUsersByDepartmentsIgnoreCase(Set.of("marketing", "marketing ops")))
                .thenReturn(List.of(staffA, staffB));

        Task task1 = createTask(101L, 4L, 3L, TaskType.FEATURE, Status.IN_PROGRESS, 9);
        task1.setDueDateTime(OffsetDateTime.now().plusDays(7));
        task1.setOwnerId(8L);
        task1.setProjectId(201L);

        Task task2 = createTask(102L, 4L, 3L, TaskType.BUG, Status.BLOCKED, 10);
        task2.setOwnerId(12L);
        task2.setProjectId(201L);
        task2.setUpdatedAt(OffsetDateTime.now().minusDays(1));

        when(taskRepository.findVisibleTasksForUsers(Set.of(8L, 12L))).thenReturn(List.of(task1, task2));

        TaskAssignee taskAssignee = new TaskAssignee();
        taskAssignee.setTaskId(102L);
        taskAssignee.setUserId(8L);
        taskAssignee.setAssignedId(managerUser.getId());

        when(taskAssigneeRepository.findByTaskIdIn(Set.of(101L, 102L)))
                .thenReturn(List.of(taskAssignee));

        when(userRepository.findByIdIn(Mockito.<List<Long>>any()))
                .thenReturn(List.of(staffA, staffB));

        Project project = new Project();
        project.setId(201L);
        project.setName("Q4 Campaign");
        project.setOwnerId(managerUser.getId());
        project.setCreatedBy(managerUser.getId());
        project.setUpdatedBy(managerUser.getId());

        when(projectRepository.findAllById(Set.of(201L))).thenReturn(List.of(project));

        var dashboard = service.getDepartmentDashboard(managerUser);

        assertThat(dashboard.getDepartment()).isEqualTo("Marketing");
        assertThat(dashboard.getIncludedDepartments()).containsExactly("Marketing", "Marketing Ops");

        assertThat(dashboard.getMetrics().getTotalTasks()).isEqualTo(2);
        assertThat(dashboard.getMetrics().getBlockedTasks()).isEqualTo(1);
        assertThat(dashboard.getMetrics().getHighPriorityTasks()).isEqualTo(1);
        assertThat(dashboard.getMetrics().getCompletionRate()).isEqualTo(50.0);

        assertThat(dashboard.getProjects()).hasSize(1);
        var projectCard = dashboard.getProjects().get(0);
        assertThat(projectCard.getProjectName()).isEqualTo("Q4 Campaign");
        assertThat(projectCard.getStatus()).isEqualTo("At Risk");
        assertThat(projectCard.getTotalTasks()).isEqualTo(2);
        assertThat(projectCard.getBlockedTasks()).isEqualTo(1);

        assertThat(dashboard.getPriorityQueue()).hasSize(1);
        assertThat(dashboard.getPriorityQueue().get(0).getId()).isEqualTo(102L);

        assertThat(dashboard.getTeamLoad()).hasSize(2);
        assertThat(dashboard.getTeamLoad().get(0).getTaskCount()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void getDepartmentDashboard_rejectsStaffRole() {
        User staffUser = new User();
        staffUser.setId(20L);
        staffUser.setUserName("staff_member");
        staffUser.setRoleType(UserType.STAFF.getCode());
        staffUser.setDepartment("Marketing");

        assertThatThrownBy(() -> service.getDepartmentDashboard(staffUser))
                .isInstanceOf(AccessDeniedException.class);
    }

    private User createStaffUser(Long id, String name, String department) {
        User user = new User();
        user.setId(id);
        user.setUserName(name);
        user.setDepartment(department);
        user.setRoleType(UserType.STAFF.getCode());
        user.setIsActive(true);
        return user;
    }

    private Task createTask(Long id,
                            Long createdBy,
                            Long updatedBy,
                            TaskType type,
                            Status status,
                            Integer priority) {
        Task task = new Task();
        task.setId(id);
        task.setCreatedBy(createdBy);
        task.setUpdatedBy(updatedBy);
        task.setTaskType(type);
        task.setStatus(status);
        task.setPriority(priority);
        task.setTitle("Task " + id);
        task.setDescription("Description " + id);
        task.setCreatedAt(OffsetDateTime.now().minusDays(2));
        task.setUpdatedAt(OffsetDateTime.now().minusDays(1));
        return task;
    }
}
