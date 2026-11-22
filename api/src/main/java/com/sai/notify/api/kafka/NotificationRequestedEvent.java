package com.sai.notify.api.kafka;

import com.sai.notify.api.domain.Category;
import com.sai.notify.api.domain.Priority;
import lombok.Builder;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Builder
public record NotificationRequestedEvent(
        UUID notificationId,
        Long userId,
        String templateId,
        Priority priority,
        Category category,
        Map<String, String> variables,
        Instant expiresAt
) {}