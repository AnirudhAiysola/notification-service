package com.sai.notify.api.controller;

import com.sai.notify.api.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)

class NotificationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    NotificationService notificationService;

    @Test
    void validRequest_returns202() throws Exception {
        when(notificationService.accept(any())).thenReturn(UUID.randomUUID());
        String json = """
            {
              "idempotencyKey": "order-123-shipped",
              "userId": 42,
              "templateId": "order_shipped",
              "priority": "LOW",
              "category": "TRANSACTIONAL",
              "variables": { "orderId": "123" }
            }
            """;

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.notificationId").exists())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void missingUserId_returns400() throws Exception {
        String json = """
            {
              "idempotencyKey": "order-123-shipped",
              "templateId": "order_shipped",
              "priority": "LOW",
              "category": "TRANSACTIONAL"
            }
            """;

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}