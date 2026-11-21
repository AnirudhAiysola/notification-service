package com.sai.notify.api.controller;

import com.sai.notify.api.dto.NotificationAcceptedResponse;
import com.sai.notify.api.dto.NotificationRequest;
import com.sai.notify.api.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public NotificationAcceptedResponse create(@Valid @RequestBody NotificationRequest request) {
        UUID id = service.accept(request);

        return new NotificationAcceptedResponse(id,"ACCEPTED");
    }
}