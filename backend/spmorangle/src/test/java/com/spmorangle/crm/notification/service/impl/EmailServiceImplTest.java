package com.spmorangle.crm.notification.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import com.spmorangle.crm.notification.service.EmailService;

import jakarta.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Tests")
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private com.spmorangle.crm.taskmanagement.service.TaskService taskService;

    @Mock
    private com.spmorangle.crm.usermanagement.service.UserManagementService userManagementService;

    @Mock
    private com.spmorangle.crm.notification.service.EmailTemplateService emailTemplateService;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender, taskService, userManagementService, emailTemplateService);
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@spmorangle.com");
        ReflectionTestUtils.setField(emailService, "frontendBaseUrl", "http://localhost:3000");
    }

    @Test
    @DisplayName("Should send plain text email successfully")
    void shouldSendPlainTextEmail() {
        // Arrange
        String toEmail = "user@example.com";
        String subject = "Test Subject";
        String body = "Test Body Content";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        assertDoesNotThrow(() -> emailService.sendEmail(toEmail, subject, body));

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should send HTML email successfully")
    void shouldSendHtmlEmail() {
        // Arrange
        String toEmail = "user@example.com";
        String subject = "Test HTML Subject";
        String htmlBody = "<h1>Test HTML Content</h1>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        var future = emailService.sendHtmlEmail(toEmail, subject, htmlBody);

        // Assert
        assertNotNull(future);
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}