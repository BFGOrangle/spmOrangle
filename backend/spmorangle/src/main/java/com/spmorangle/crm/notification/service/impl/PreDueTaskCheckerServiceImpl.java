package com.spmorangle.crm.notification.service.impl;

import com.spmorangle.crm.notification.service.PreDueTaskCheckerService;
import com.spmorangle.crm.notification.service.PreDueTaskEmailService;
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
public class PreDueTaskCheckerServiceImpl implements PreDueTaskCheckerService {

    private final TaskRepository taskRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final PreDueTaskEmailService preDueTaskEmailService;

    private static final Long SYSTEM_USER_ID = -1L;

    @Override
    @Scheduled(fixedRateString = "${predue.check.rate-ms:60000}")
    @Transactional
    public void checkAndNotifyPredueTasks() {
        OffsetDateTime threshold24h = OffsetDateTime.now(ZoneOffset.UTC).plusHours(24);
        OffsetDateTime threshold12h = OffsetDateTime.now(ZoneOffset.UTC).plusHours(12);

        log.info("Checking for tasks pre due before 24h: {} and rescheduled before 12h: {}", threshold24h, threshold12h);

        // Get all candidates within 24 hours (covers both thresholds)
        List<Task> candidates = taskRepository.findByDueDateTimeBefore(threshold24h);
        if (candidates.isEmpty()) {
            log.debug("No pre due candidate tasks found");
            return;
        }

        for (Task task : candidates) {
            // Determine threshold based on reschedule status
            boolean isRescheduled = Boolean.TRUE.equals(task.getIsRescheduled());
            OffsetDateTime applicableThreshold = isRescheduled ? threshold12h : threshold24h;
            int hoursUntilDue = isRescheduled ? 12 : 24;

            // Skip if task is outside the applicable threshold
            if (task.getDueDateTime() != null && task.getDueDateTime().isAfter(applicableThreshold)) {
                log.debug("Skipping task {} - due date {} is outside {} threshold",
                        task.getId(), task.getDueDateTime(), isRescheduled ? "12h" : "24h");
                continue;
            }

            // Skip if pre due notification has already been sent for this schedule state
            if (Boolean.TRUE.equals(task.getHasSentPreDue())) {
                log.debug("Skipping task {} - pre due notification already sent", task.getId());
                continue;
            }

            // Skip if completed
            if (task.getStatus() != null) {
                String s = task.getStatus().name();
                if ("COMPLETED".equalsIgnoreCase(s)) {
                    log.debug("Skipping task {} because status is {}", task.getId(), s);
                    continue;
                }
            }

            // Get assignees
            List<TaskAssignee> assignees = taskAssigneeRepository.findByTaskId(task.getId());
            if (assignees == null || assignees.isEmpty()) {
                log.warn("Task {} is pre due but has no assignees", task.getId());
                continue;
            }

            // Send emails to all assignees
            boolean emailSentSuccessfully = true;
            for (TaskAssignee assignee : assignees) {
                try {
                    preDueTaskEmailService.sendPreDueTaskEmail(task, assignee, hoursUntilDue);
                    log.info("Triggered {} pre due email for task {} -> user {}",
                            isRescheduled ? "rescheduled" : "standard", task.getId(), assignee.getUserId());
                } catch (Exception ex) {
                    log.error("Failed to send pre due email for task {} to {}: {}",
                            task.getId(), assignee.getUserId(), ex.getMessage(), ex);
                    emailSentSuccessfully = false;
                }
            }

            // Mark as sent only if all emails were sent successfully
            if (emailSentSuccessfully) {
                task.setHasSentPreDue(true);
                task.setUpdatedBy(SYSTEM_USER_ID);
                taskRepository.save(task);
                log.info("Marked task {} as pre due notification sent ({})",
                        task.getId(), isRescheduled ? "rescheduled 12h" : "standard 24h");
            }
        }
    }
}
