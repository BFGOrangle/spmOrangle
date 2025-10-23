package com.spmorangle.crm.notification.service.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.spmorangle.crm.notification.dto.DailyDigestDto;
import com.spmorangle.crm.notification.service.EmailService;
import com.spmorangle.crm.notification.service.EmailTemplateService;
import com.spmorangle.crm.taskmanagement.dto.TaskResponseDto;
import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.service.TaskService;
import com.spmorangle.crm.usermanagement.dto.UserResponseDto;
import com.spmorangle.crm.usermanagement.service.UserManagementService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TaskService taskService;
    private final UserManagementService userManagementService;
    private final EmailTemplateService emailTemplateService;
    
    @Value("${spring.mail.from:noreply@spmorangle.com}")
    private String fromEmail;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Async("emailExecutor")
    @Override
    public CompletableFuture<Void> sendEmail(String toEmail, String subject, String body) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body);

            mailSender.send(mimeMessage);
            
            log.info("Email sent successfully to: {}", toEmail);
            return CompletableFuture.completedFuture(null);
            
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);

        }
    }

    @Async("emailExecutor")
    @Override
    public CompletableFuture<Void> sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML content
            
            mailSender.send(mimeMessage);
            
            log.info("HTML email sent successfully to: {}", toEmail);
            return CompletableFuture.completedFuture(null);
            
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}: {}", toEmail, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }


    @Override
    @Scheduled(cron = "0 56 23 * * ?")
    public void sendDailyEmail(){
        try {
            log.info("Starting daily digest email job");
            long startTime = System.currentTimeMillis();

            OffsetDateTime now = OffsetDateTime.now();
            OffsetDateTime startDate = now.plusDays(1).toLocalDate().atStartOfDay(now.getOffset()).toOffsetDateTime();
            OffsetDateTime endDate = startDate.plusDays(1);

            log.info("Finding tasks due between {} and {}", startDate, endDate);

            List<UserResponseDto> users = userManagementService.getAllUsers();
            log.info("Processing {} users for daily digest", users.size());
            int emailsSent = 0;

            for(UserResponseDto user : users) {
                try {
                    if(Boolean.FALSE.equals(user.isActive()) || user.email() == null || user.email().trim().isEmpty()) {
                        log.debug("Skipping user {} - inactive or no email", user.id());
                        continue;
                    }

                    List<TaskResponseDto> tasksDueTmr = taskService.getUserTasksDueTomorrowForDigest(
                        user.id(),
                        startDate,
                        endDate
                    );

                    if(tasksDueTmr.isEmpty()) {
                        log.debug("No tasks due tomorrow for user {}", user.email());
                        continue;
                    }

                    long todoCount = tasksDueTmr.stream()
                        .filter(t -> t.getStatus() == Status.TODO).count();
                    long inProgressCount = tasksDueTmr.stream()
                        .filter(t -> t.getStatus() == Status.IN_PROGRESS).count();
                    long blockedCount = tasksDueTmr.stream()
                        .filter(t -> t.getStatus() == Status.BLOCKED).count();

                    DailyDigestDto digestDto = DailyDigestDto.builder()
                        .user(user)
                        .tasks(tasksDueTmr)
                        .frontendBaseUrl(frontendBaseUrl)
                        .todoCount(todoCount)
                        .inProgressCount(inProgressCount)
                        .blockedCount(blockedCount)
                        .totalPending(tasksDueTmr.size())
                        .build();

                    String htmlBody = emailTemplateService.generateDailyDigestEmail(digestDto);
                    String subject = "Daily Task Digest - " + tasksDueTmr.size() +
                            " Task" + (tasksDueTmr.size() != 1 ? "s" : "") + " Due Tomorrow";

                    sendHtmlEmail(user.email(), subject, htmlBody)
                        .exceptionally(ex -> {
                            log.error("Failed to send digest email to {}: {}", user.email(), ex.getMessage());
                            return null;
                        });

                    emailsSent++;
                    log.debug("Queued digest email for {} ({} tasks)", user.email(), tasksDueTmr.size());

                } catch (Exception e) {
                    log.error("Error processing digest for user {}: {}",
                        user.id(), e.getMessage(), e);
                }
            }

            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("Daily digest job completed. Queued {} emails in {}ms", emailsSent, elapsedTime);

        } catch(Exception e) {
            log.error("Error in sending daily digest email", e);
        }
    }
}