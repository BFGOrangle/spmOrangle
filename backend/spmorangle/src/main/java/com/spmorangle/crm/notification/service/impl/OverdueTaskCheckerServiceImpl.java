package com.spmorangle.crm.notification.service.impl;

import com.spmorangle.crm.notification.service.OverdueTaskCheckerService;
import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.model.TaskAssignee;
import com.spmorangle.crm.taskmanagement.repository.TaskAssigneeRepository;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OverdueTaskCheckerServiceImpl implements OverdueTaskCheckerService {

    private final TaskRepository taskRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final OverdueTaskEmailServiceImpl overdueTaskEmailService;

    @Override
    @Scheduled(fixedRateString = "${overdue.check.rate-ms:60000}")
    @Transactional(readOnly = true)
    public void checkAndNotifyOverdueTasks() {
        OffsetDateTime threshold = OffsetDateTime.now(ZoneOffset.UTC).minusHours(24);
        log.info("Checking for tasks overdue before {}", threshold);

        List<Task> candidates = taskRepository.findByDueDateTimeBefore(threshold);
        if (candidates.isEmpty()) {
            log.debug("No overdue candidate tasks found");
            return;
        }

        for (Task task : candidates) {
            if (task.getStatus() != null) {
                String s = task.getStatus().name();
                if ("COMPLETED".equalsIgnoreCase(s)) {
                    log.debug("Skipping task {} because status is {}", task.getId(), s);
                    continue;
                }
            }

            List<TaskAssignee> assignees = taskAssigneeRepository.findByTaskId(task.getId());
            if (assignees == null || assignees.isEmpty()) {
                log.warn("Task {} is overdue but has no assignees", task.getId());
                continue;
            }

            for (TaskAssignee assignee : assignees) {
                try {
                    overdueTaskEmailService.sendOverdueTaskEmail(task, assignee);
                    log.info("Triggered overdue email for task {} -> user {}", task.getId(), assignee.getUserId());
                } catch (Exception ex) {
                    log.error("Failed to send overdue email for task {} to {}: {}", task.getId(), assignee.getUserId(), ex.getMessage(), ex);
                }
            }
        }
    }
}
