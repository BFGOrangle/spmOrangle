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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PreDueTaskCheckerServiceImpl implements PreDueTaskCheckerService {

    private final TaskRepository taskRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final PreDueTaskEmailService preDueTaskEmailService;

    @Override
    @Scheduled(fixedRateString = "${predue.check.rate-ms:60000}")
    @Transactional
    public void checkAndNotifyPredueTasks() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        // Default threshold keeps previous behavior for non-rescheduled tasks (24 hours)
        OffsetDateTime threshold = now.plusHours(24);
        // For rescheduled tasks we need to consider a wider window so their 12-hour pre-due may fall within the scheduler's scan window.
        // The scheduler uses `threshold = now.plusHours(24)` for normal candidates. To capture rescheduled tasks whose
        // pre-due time (dueDateTime - 12h) falls within that same threshold window, we need to look ahead by an additional
        // 12 hours from the threshold (threshold + 12h). With the current threshold of 24h this equals now+36h.
        final int PRE_DUE_HOURS = 12;
        OffsetDateTime thresholdPlus12 = threshold.plusHours(PRE_DUE_HOURS);
        log.info("Checking for tasks pre due before {} (now={})", threshold, now);

        // Fetch normal candidates (due within 24h)
        List<Task> candidates = new ArrayList<>(taskRepository.findByDueDateTimeBefore(threshold));
        // Fetch rescheduled tasks whose dueDateTime falls within a larger window so their 12-hour pre-due may be within 24h
        List<Task> rescheduledCandidates = taskRepository.findRescheduledTasksDueBefore(thresholdPlus12);

        // Merge lists while deduplicating by task id
        Set<Long> seenIds = new HashSet<>();
        for (Task t : candidates) {
            seenIds.add(t.getId());
        }
        for (Task t : rescheduledCandidates) {
            if (!seenIds.contains(t.getId())) {
                candidates.add(t);
                seenIds.add(t.getId());
            }
        }

        if (candidates.isEmpty()) {
            log.debug("No pre due candidate tasks found");
            return;
        }

        for (Task task : candidates) {
            // Skip if task has no due date
            if (task.getDueDateTime() == null) {
                log.debug("Skipping task {} - no due date set", task.getId());
                continue;
            }

            // Skip completed tasks
            if (task.getStatus() != null && "COMPLETED".equalsIgnoreCase(task.getStatus().name())) {
                log.debug("Skipping task {} because status is {}", task.getId(), task.getStatus().name());
                continue;
            }

            // If task was rescheduled, cancel previous reminder and ensure we only send at 12 hours before the new due date
            boolean isRescheduled = Boolean.TRUE.equals(task.getIsRescheduled());
            OffsetDateTime preDueForRescheduled;
            if (isRescheduled) {
                preDueForRescheduled = task.getDueDateTime().minusHours(12);
                // If a previous pre-due was already marked sent, reset it so a new reminder can be sent
                if (Boolean.TRUE.equals(task.getHasSentPreDue())) {
                    task.setHasSentPreDue(false);
                    // Persist the reset immediately so other processes see the change
                    taskRepository.save(task);
                    log.info("Reset pre-due sent flag for rescheduled task {}", task.getId());
                }

                // If it's not yet time to send the rescheduled pre-due (12h before new due), skip for now
                if (now.isBefore(preDueForRescheduled)) {
                    log.debug("Not yet time to send rescheduled pre-due for task {} (will send at {})", task.getId(), preDueForRescheduled);
                    continue;
                }
            } else {
                // Non-rescheduled tasks: skip if pre-due already sent
                if (Boolean.TRUE.equals(task.getHasSentPreDue())) {
                    log.debug("Skipping task {} - pre due notification already sent", task.getId());
                    continue;
                }
            }

            // For both rescheduled (now at/after 12h before) and regular tasks (due within 24h), proceed to notify
            List<TaskAssignee> assignees = taskAssigneeRepository.findByTaskId(task.getId());
            if (assignees == null || assignees.isEmpty()) {
                log.warn("Task {} is pre due but has no assignees", task.getId());
                continue;
            }

            boolean emailSentSuccessfully = true;
            for (TaskAssignee assignee : assignees) {
                try {
                    preDueTaskEmailService.sendPreDueTaskEmail(task, assignee);
                    log.info("Triggered pre due email for task {} -> user {}", task.getId(), assignee.getUserId());
                } catch (Exception ex) {
                    log.error("Failed to send pre due email for task {} to {}: {}", task.getId(), assignee.getUserId(), ex.getMessage(), ex);
                    emailSentSuccessfully = false;
                }
            }

            // Mark as sent only if all emails were sent successfully
            if (emailSentSuccessfully) {
                task.setHasSentPreDue(true);
                // If this was a rescheduled task, clear the rescheduled flag as we've handled it
                if (isRescheduled) {
                    task.setIsRescheduled(false);
                }
                taskRepository.save(task);
                log.info("Marked task {} as pre due notification sent{}", task.getId(), (isRescheduled ? " and cleared rescheduled flag" : ""));
            }
        }
    }
}
