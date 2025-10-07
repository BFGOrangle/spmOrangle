package com.spmorangle.crm.notification.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.spmorangle.common.enums.NotificationType;
import com.spmorangle.crm.notification.enums.Channel;
import com.spmorangle.crm.notification.enums.Priority;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @ElementCollection(targetClass = Channel.class)
    @CollectionTable(name = "notification_channels", 
                    joinColumns = @JoinColumn(name = "notification_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "channel")
    @Builder.Default
    private List<Channel> channels = new ArrayList<>();

    @Column(name = "read_status", nullable = false)
    @Builder.Default
    private Boolean readStatus = false;

    @Column(name = "dismissed_status", nullable = false)
    @Builder.Default
    private Boolean dismissedStatus = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Column(name = "link", length = 500)
    private String link;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "dismissed_at")
    private Instant dismissedAt;

    // Helper methods
    public void markAsRead() {
        this.readStatus = true;
        this.readAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void markAsDismissed() {
        this.dismissedStatus = true;
        this.dismissedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void addChannel(Channel channel) {
        if (this.channels == null) {
            this.channels = new ArrayList<>();
        }
        if (!this.channels.contains(channel)) {
            this.channels.add(channel);
        }
    }

    public boolean isUnread() {
        return !Boolean.TRUE.equals(this.readStatus);
    }

    public boolean isActive() {
        return !Boolean.TRUE.equals(this.dismissedStatus);
    }
}