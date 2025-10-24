package com.spmorangle.crm.notification.service;

import java.util.concurrent.CompletableFuture;

public interface EmailService {
    
    /**
     * Send email notification
     * @param toEmail recipient email address
     * @param subject email subject
     * @param body email body content
     */
    CompletableFuture<Void> sendEmail(String toEmail, String subject, String body);
    
    /**
     * Send HTML email notification  
     * @param toEmail recipient email address
     * @param subject email subject
     * @param htmlBody HTML email body content
     */
    CompletableFuture<Void> sendHtmlEmail(String toEmail, String subject, String htmlBody);

    void sendDailyEmail();
}