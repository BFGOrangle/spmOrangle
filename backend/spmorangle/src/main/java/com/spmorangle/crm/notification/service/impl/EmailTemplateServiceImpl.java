package com.spmorangle.crm.notification.service.impl;

import org.springframework.stereotype.Service;

import com.spmorangle.crm.notification.dto.DailyDigestDto;
import com.spmorangle.crm.notification.dto.NotificationDto;
import com.spmorangle.crm.notification.service.EmailTemplateService;
import com.spmorangle.crm.taskmanagement.dto.TaskResponseDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailTemplateServiceImpl implements EmailTemplateService {

    @Override
    public String generateEmailBody(NotificationDto notification) {
        StringBuilder emailBody = new StringBuilder();
        
        // HTML Email Template
        emailBody.append("<!DOCTYPE html>");
        emailBody.append("<html>");
        emailBody.append("<head>");
        emailBody.append("<meta charset='UTF-8'>");
        emailBody.append("<style>");
        emailBody.append("body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4; }");
        emailBody.append(".container { max-width: 600px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }");
        emailBody.append(".header { background-color: #007bff; color: white; padding: 15px; border-radius: 5px; margin-bottom: 20px; }");
        emailBody.append(".content { margin: 20px 0; }");
        emailBody.append(".footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; color: #666; font-size: 12px; }");
        emailBody.append(".button { display: inline-block; background-color: #007bff; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; margin: 10px 0; }");
        emailBody.append("</style>");
        emailBody.append("</head>");
        emailBody.append("<body>");
        
        emailBody.append("<div class='container'>");
        
        // Header
        emailBody.append("<div class='header'>");
        emailBody.append("<h2>").append(notification.getSubject()).append("</h2>");
        emailBody.append("</div>");
        
        // Content
        emailBody.append("<div class='content'>");
        emailBody.append("<p>Hello,</p>");
        emailBody.append("<p>").append(notification.getMessage()).append("</p>");
        
        // Add action button if link exists
        if (notification.getLink() != null && !notification.getLink().trim().isEmpty()) {
            emailBody.append("<p>");
            emailBody.append("<a href='").append(notification.getLink()).append("' class='button'>View Details</a>");
            emailBody.append("</p>");
        }
        
        emailBody.append("</div>");
        
        // Footer
        emailBody.append("<div class='footer'>");
        emailBody.append("<p>Best regards,<br>");
        emailBody.append("SPM Orangle Team</p>");
        emailBody.append("<p><small>This is an automated notification. Please do not reply to this email.</small></p>");
        emailBody.append("</div>");
        
        emailBody.append("</div>");
        emailBody.append("</body>");
        emailBody.append("</html>");
        
        return emailBody.toString();
    }

    @Override
    public String generatePlainTextBody(NotificationDto notification) {
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("Hello,\n\n");
        emailBody.append(notification.getMessage()).append("\n\n");
        
        if (notification.getLink() != null && !notification.getLink().trim().isEmpty()) {
            emailBody.append("Click here to view: ").append(notification.getLink()).append("\n\n");
        }
        
        emailBody.append("Best regards,\n");
        emailBody.append("SPM Orangle Team\n\n");
        emailBody.append("This is an automated notification. Please do not reply to this email.");
        
        return emailBody.toString();
    }

    @Override
    public String generateDailyDigestEmail(DailyDigestDto digestDto) {
        StringBuilder html = new StringBuilder();
        
        // HTML structure
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<style>");
        
        // Styles
        html.append("body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); overflow: hidden; }");
        html.append(".header { background-color: #ff6b35; color: white; padding: 30px 20px; text-align: center; }");
        html.append(".header h1 { margin: 0; font-size: 24px; }");
        html.append(".header p { margin: 10px 0 0 0; opacity: 0.9; }");
        html.append(".summary { background-color: #fff3e0; padding: 20px; margin: 20px; border-radius: 8px; border-left: 4px solid #ff6b35; }");
        html.append(".summary h2 { margin-top: 0; color: #ff6b35; font-size: 18px; }");
        html.append(".summary-stats { display: flex; justify-content: space-around; margin-top: 15px; gap: 30px; }");
        html.append(".stat { text-align: center; flex: 1; }");
        html.append(".stat-number { font-size: 24px; font-weight: bold; color: #ff6b35; }");
        html.append(".stat-label { font-size: 12px; color: #666; margin-top: 5px; }");
        html.append(".task-list { padding: 0 20px 20px 20px; }");
        html.append(".task-list h2 { color: #333; font-size: 18px; margin-bottom: 15px; }");
        html.append(".task { border-left: 4px solid #ff6b35; padding: 15px; margin-bottom: 15px; background: #f9f9f9; border-radius: 4px; }");
        html.append(".task h3 { margin: 0 0 10px 0; color: #333; font-size: 16px; }");
        html.append(".task-meta { font-size: 14px; color: #666; margin: 8px 0; }");
        html.append(".task-meta strong { color: #333; }");
        html.append(".status { display: inline-block; padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: bold; margin: 5px 0; }");
        html.append(".status-todo { background-color: #e3f2fd; color: #1976d2; }");
        html.append(".status-in-progress { background-color: #fff3e0; color: #f57c00; }");
        html.append(".status-blocked { background-color: #ffebee; color: #c62828; }");
        html.append(".task-link { display: inline-block; margin-top: 10px; color: #ff6b35; text-decoration: none; font-weight: bold; }");
        html.append(".task-link:hover { text-decoration: underline; }");
        html.append(".footer { background-color: #f4f4f4; padding: 20px; text-align: center; color: #666; font-size: 12px; }");
        html.append(".footer p { margin: 5px 0; }");
        
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        html.append("<div class='container'>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<h1>Daily Task Digest</h1>");
        html.append("<p>Hello ").append(digestDto.getUser().username()).append(", here are your tasks due tomorrow</p>");
        html.append("</div>");
        
        // Summary Section
        html.append("<div class='summary'>");
        html.append("<h2>Summary</h2>");
        html.append("<p>You have <strong>").append(digestDto.getTotalPending())
            .append(" pending task").append(digestDto.getTotalPending() != 1 ? "s" : "")
            .append("</strong> due tomorrow.</p>");
        
        html.append("<div class='summary-stats'>");
        html.append("<div class='stat'>");
        html.append("<div class='stat-number'>").append(digestDto.getTodoCount()).append("</div>");
        html.append("<div class='stat-label'>To Do</div>");
        html.append("</div>");
        html.append("<div class='stat'>");
        html.append("<div class='stat-number'>").append(digestDto.getInProgressCount()).append("</div>");
        html.append("<div class='stat-label'>In Progress</div>");
        html.append("</div>");
        html.append("<div class='stat'>");
        html.append("<div class='stat-number'>").append(digestDto.getBlockedCount()).append("</div>");
        html.append("<div class='stat-label'>Blocked</div>");
        html.append("</div>");
        html.append("</div>");
        html.append("</div>");
        
        // Task List
        html.append("<div class='task-list'>");
        html.append("<h2>Your Tasks</h2>");
        
        for (TaskResponseDto task : digestDto.getTasks()) {
            html.append("<div class='task'>");
            html.append("<h3>").append(escapeHtml(task.getTitle())).append("</h3>");
            
            // Status badge
            String statusClass = switch (task.getStatus()) {
                case TODO -> "status-todo";
                case IN_PROGRESS -> "status-in-progress";
                case BLOCKED -> "status-blocked";
                default -> "";
            };
            String statusLabel = task.getStatus().toString().replace("_", " ");
            html.append("<span class='status ").append(statusClass).append("'>")
                .append(statusLabel)
                .append("</span>");
            
            // Due time
            if (task.getDueDateTime() != null) {
                html.append("<div class='task-meta'>");
                html.append("<strong>Due:</strong> ")
                    .append(task.getDueDateTime().toString());
                html.append("</div>");
            }
            
            // Project info
            if (task.getProjectName() != null) {
                html.append("<div class='task-meta'>");
                html.append("<strong>Project:</strong> ").append(escapeHtml(task.getProjectName()));
                html.append("</div>");
            }
            
            // Task link
            String taskUrl = digestDto.getFrontendBaseUrl() + "/tasks/" + task.getId();
            html.append("<a href='").append(taskUrl).append("' class='task-link'>View Task →</a>");
            
            html.append("</div>");
        }
        
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p><strong>SPM Orangle Team</strong></p>");
        html.append("<p>This is an automated daily digest sent at 09:00. Tasks reflect your current assignments.</p>");
        html.append("<p><small>Please do not reply to this email.</small></p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}