package com.spmorangle.crm.notification.service.impl;

import org.springframework.stereotype.Service;

import com.spmorangle.crm.notification.dto.NotificationDto;
import com.spmorangle.crm.notification.service.EmailTemplateService;

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
}