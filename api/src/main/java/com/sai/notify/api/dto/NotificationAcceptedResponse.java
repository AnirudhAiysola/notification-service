package com.sai.notify.api.dto;

import java.util.UUID;

public record NotificationAcceptedResponse(UUID notificationId, String status) {}