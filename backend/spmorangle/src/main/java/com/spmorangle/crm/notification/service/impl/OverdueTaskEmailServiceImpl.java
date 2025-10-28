package com.spmorangle.crm.notification.service.impl;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.spmorangle.crm.notification.service.EmailService;
import com.spmorangle.crm.notification.service.OverdueTaskEmailService;
import com.spmorangle.crm.usermanagement.service.UserManagementService;
import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.model.TaskAssignee;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OverdueTaskEmailServiceImpl implements OverdueTaskEmailService {

    private final EmailService emailService; // Uses composition
    private final UserManagementService userManagementService;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");
    private static final ZoneId SINGAPORE_ZONE = ZoneId.of("Asia/Singapore"); // UTC+8

    public String getAssigneeEmail(TaskAssignee assignee) {
        long assigneeId = assignee.getUserId();
        var assigneeProfile = userManagementService.getUserById(assigneeId);
        return assigneeProfile.email();
    }

    public String getAssigneeName(TaskAssignee assignee) {
        long assigneeId = assignee.getUserId();
        var assigneeProfile = userManagementService.getUserById(assigneeId);
        return assigneeProfile.username();
    }

    @Override
    public void sendOverdueTaskEmail(Task task, TaskAssignee assignee) {
        String subject = String.format("Overdue Task: %s", task.getTitle());
        String htmlBody = buildOverdueTaskEmailHtml(task, assignee);

        String assigneeEmail = getAssigneeEmail(assignee);

        try {
            // Wait for async email to complete - this will throw if email fails
            emailService.sendHtmlEmail(assigneeEmail, subject, htmlBody).join();

            log.info("Sent overdue task email for task {} to {}",
                    task.getId(), assigneeEmail);
        } catch (Exception e) {
            log.error("Failed to send overdue task email for task {} to {}: {}",
                    task.getId(), assigneeEmail, e.getMessage());
            throw e; // Re-throw so checker knows email failed
        }
    }

    @Override
    public void sendMultipleOverdueTasksEmail(List<Task> tasks, TaskAssignee assignee) {
        if (tasks.isEmpty()) {
            return;
        }

        String assigneeEmail = getAssigneeEmail(assignee);

        String subject = String.format("You have %d overdue tasks", tasks.size());
        String htmlBody = buildMultipleOverdueTasksEmailHtml(tasks, assignee);

        try {
            // Wait for async email to complete
            emailService.sendHtmlEmail(assigneeEmail, subject, htmlBody).join();

            log.info("Sent overdue tasks summary email ({} tasks) to {}",
                    tasks.size(), assigneeEmail);
        } catch (Exception e) {
            log.error("Failed to send overdue tasks summary email ({} tasks) to {}: {}",
                    tasks.size(), assigneeEmail, e.getMessage());
            throw e;
        }
    }

    private String buildOverdueTaskEmailHtml(Task task, TaskAssignee assignee) {
        // Convert UTC time to Singapore time (UTC+8)
        String formattedDueDate = task.getDueDateTime()
                .atZoneSameInstant(SINGAPORE_ZONE)
                .format(DATE_FORMATTER);

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2 style="color: #dc3545;">Task Overdue Reminder</h2>
                <p>Hi %s,</p>
                <p>The following task is overdue:</p>
                <div style="border-left: 4px solid #dc3545; padding-left: 15px; margin: 20px 0; background-color: #fff3cd; padding: 15px;">
                    <h3 style="margin-top: 0;">%s</h3>
                    <p><strong>Due Date:</strong> %s</p>
                    <p><strong>Status:</strong> %s</p>
                    %s
                </div>
                <p>Please take action as soon as possible.</p>
                <p>Best regards,<br><strong>SPM Orange Team</strong></p>
            </body>
            </html>
            """,
                getAssigneeName(assignee),
                task.getTitle(),
                formattedDueDate,
                task.getStatus(),
                task.getDescription() != null
                        ? "<p><strong>Description:</strong> " + task.getDescription() + "</p>"
                        : ""
        );
    }

    private String buildMultipleOverdueTasksEmailHtml(List<Task> tasks, TaskAssignee assignee) {
        StringBuilder taskList = new StringBuilder();

        for (Task task : tasks) {
            // Convert UTC time to Singapore time (UTC+8)
            String formattedDueDate = task.getDueDateTime()
                    .atZoneSameInstant(SINGAPORE_ZONE)
                    .format(DATE_FORMATTER);

            taskList.append(String.format("""
                <div style="border-left: 4px solid #dc3545; padding-left: 15px; margin: 10px 0; background-color: #f8f9fa; padding: 10px;">
                    <h4 style="margin: 0 0 10px 0;">%s</h4>
                    <p style="margin: 5px 0;"><strong>Due Date:</strong> %s</p>
                    <p style="margin: 5px 0;"><strong>Status:</strong> %s</p>
                </div>
                """,
                    task.getTitle(),
                    formattedDueDate,
                    task.getStatus()
            ));
        }

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2 style="color: #dc3545;">Multiple Tasks Overdue</h2>
                <p>Hi %s,</p>
                <p>You have <strong>%d overdue tasks</strong> that require your attention:</p>
                <div style="margin: 20px 0;">
                    %s
                </div>
                <p>Please review and prioritize these tasks.</p>
                <p>Best regards,<br><strong>SPM Orange Team</strong></p>
            </body>
            </html>
            """,
                getAssigneeName(assignee),
                tasks.size(),
                taskList
        );
    }
}