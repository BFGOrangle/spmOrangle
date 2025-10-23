package com.spmorangle.crm.notification.service;

import com.spmorangle.crm.notification.dto.DailyDigestDto;
import com.spmorangle.crm.notification.dto.NotificationDto;

public interface EmailTemplateService {
    
    /**
     * Generate HTML email body from notification
     * @param notification the notification to convert
     * @return formatted HTML email body
     */
    String generateEmailBody(NotificationDto notification);
    
    /**
     * Generate plain text email body from notification
     * @param notification the notification to convert  
     * @return formatted plain text email body
     */
    String generatePlainTextBody(NotificationDto notification);

    String generateDailyDigestEmail(DailyDigestDto dailyDigestDto);
}