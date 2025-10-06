package com.spmorangle.crm.notification.service;

public interface EmailService {
    
    /**
     * Send email notification
     * @param toEmail recipient email address
     * @param subject email subject
     * @param body email body content
     */
    void sendEmail(String toEmail, String subject, String body);
    
    /**
     * Send HTML email notification  
     * @param toEmail recipient email address
     * @param subject email subject
     * @param htmlBody HTML email body content
     */
    void sendHtmlEmail(String toEmail, String subject, String htmlBody);
}