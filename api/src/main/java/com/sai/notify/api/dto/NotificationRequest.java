package com.sai.notify.api.dto;


import com.sai.notify.api.domain.Category;
import com.sai.notify.api.domain.Priority;
import jakarta.validation.constraints.*;

import java.util.Map;

public record NotificationRequest(
        @NotBlank @Size(max = 200)
        String idempotencyKey,

        @NotNull @Positive
        Long userId,

        @NotBlank @Size(max = 100)
        String templateId,

        @NotNull
        Priority priority,

        @NotNull
        Category category,

        // optional: no annotation needed
        Map<String, String> variables,

        @Positive
        Long ttlSeconds
) {}