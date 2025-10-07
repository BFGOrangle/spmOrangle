package com.spmorangle.common.events;

import com.spmorangle.common.enums.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class BaseNotificationEvent {
    protected final NotificationType notificationType;
    protected final Long recipientId;
}
