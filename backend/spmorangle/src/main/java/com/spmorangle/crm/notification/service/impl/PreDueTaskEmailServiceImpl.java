package com.spmorangle.crm.notification.service.impl;

import com.spmorangle.crm.notification.service.EmailService;
import com.spmorangle.crm.notification.service.PreDueTaskEmailService;
import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.model.TaskAssignee;
import com.spmorangle.crm.usermanagement.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreDueTaskEmailServiceImpl implements PreDueTaskEmailService {

    private final EmailService emailService; // Uses composition
    private final UserManagementService userManagementService;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");
    private static final ZoneId SINGAPORE_ZONE = ZoneId.of("Asia/Singapore"); // UTC+8

    @Override
    public void sendPreDueTaskEmail(Task task, TaskAssignee assignee, int hoursUntilDue) {
        String subject = String.format("Pre Due Task: %s", task.getTitle());
        String htmlBody = buildPreDueTaskEmailHtml(task, assignee, hoursUntilDue);

        String assigneeEmail = userManagementService.getAssigneeEmail(assignee);

        try {
            // Wait for async email to complete - this will throw if email fails
            emailService.sendHtmlEmail(assigneeEmail, subject, htmlBody).join();

            log.info("Sent pre due task email ({} hours) for task {} to {}",
                    hoursUntilDue, task.getId(), assigneeEmail);
        } catch (Exception e) {
            log.error("Failed to send pre due task email for task {} to {}: {}",
                    task.getId(), assigneeEmail, e.getMessage());
            throw e; // Re-throw so checker knows email failed
        }
    }

    @Override
    public void sendMultiplePreDueTasksEmail(List<Task> tasks, TaskAssignee assignee) {
        if (tasks.isEmpty()) {
            return;
        }

        String assigneeEmail = userManagementService.getAssigneeEmail(assignee);

        String subject = String.format("You have %d pre due tasks", tasks.size());
        String htmlBody = buildMultiplePreDueTasksEmailHtml(tasks, assignee);

        try {
            // Wait for async email to complete
            emailService.sendHtmlEmail(assigneeEmail, subject, htmlBody).join();

            log.info("Sent pre due tasks summary email ({} tasks) to {}",
                    tasks.size(), assigneeEmail);
        } catch (Exception e) {
            log.error("Failed to send pre due tasks summary email ({} tasks) to {}: {}",
                    tasks.size(), assigneeEmail, e.getMessage());
            throw e;
        }
    }

    private String buildPreDueTaskEmailHtml(Task task, TaskAssignee assignee, int hoursUntilDue) {
        // Convert UTC time to Singapore time (UTC+8)
        String formattedDueDate = task.getDueDateTime()
                .atZoneSameInstant(SINGAPORE_ZONE)
                .format(DATE_FORMATTER);

        String dueTimeMessage = hoursUntilDue == 12
                ? "due in less than 12 hours (rescheduled)"
                : "due in less than 24 hours";

        String taskUrl = String.format("https://spm-orangle.vercel.app/tasks/%d", task.getId());

        return String.format("""
        <html>
        <body style="font-family: Arial, sans-serif;">
            <h2 style="color: #dc3545;">Task Pre Due Reminder</h2>
            <p>Hi %s,</p>
            <p>The following task is %s:</p>
            <div style="border-left: 4px solid #dc3545; padding-left: 15px; margin: 20px 0; background-color: #fff3cd; padding: 15px;">
                <h3 style="margin-top: 0;">%s</h3>
                <p><strong>Due Date:</strong> %s</p>
                <p><strong>Status:</strong> %s</p>
                %s
                <p><a href="%s" style="display: inline-block; margin-top: 10px; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px;">View Task</a></p>
            </div>
            <p>Please prioritise this task.</p>
            <p>Best regards,<br><strong>SPM Orange Team</strong></p>
        </body>
        </html>
        """,
                userManagementService.getAssigneeName(assignee),
                dueTimeMessage,
                task.getTitle(),
                formattedDueDate,
                task.getStatus(),
                task.getDescription() != null
                        ? "<p><strong>Description:</strong> " + task.getDescription() + "</p>"
                        : "",
                taskUrl
        );
    }

    private String buildMultiplePreDueTasksEmailHtml(List<Task> tasks, TaskAssignee assignee) {
        StringBuilder taskList = new StringBuilder();

        for (Task task : tasks) {
            // Convert UTC time to Singapore time (UTC+8)
            String formattedDueDate = task.getDueDateTime()
                    .atZoneSameInstant(SINGAPORE_ZONE)
                    .format(DATE_FORMATTER);

            String taskUrl = String.format("https://spm-orangle.vercel.app/tasks/%d", task.getId());

            taskList.append(String.format("""
                <div style="border-left: 4px solid #dc3545; padding-left: 15px; margin: 10px 0; background-color: #f8f9fa; padding: 10px;">
                    <h4 style="margin: 0 0 10px 0;">%s</h4>
                    <p style="margin: 5px 0;"><strong>Due Date:</strong> %s</p>
                    <p style="margin: 5px 0;"><strong>Status:</strong> %s</p>
                    <p style="margin: 5px 0;"><a href="%s" style="color: #007bff; text-decoration: none;">View Task →</a></p>
                </div>
                """,
                    task.getTitle(),
                    formattedDueDate,
                    task.getStatus(),
                    taskUrl
            ));
        }

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2 style="color: #dc3545;">Multiple Tasks Due in 24 Hours</h2>
                <p>Hi %s,</p>
                <p>You have <strong>%d pre due tasks</strong> that require your attention:</p>
                <div style="margin: 20px 0;">
                    %s
                </div>
                <p>Please review and prioritize these tasks.</p>
                <p>Best regards,<br><strong>SPM Orange Team</strong></p>
            </body>
            </html>
            """,
                userManagementService.getAssigneeName(assignee),
                tasks.size(),
                taskList
        );
    }
}