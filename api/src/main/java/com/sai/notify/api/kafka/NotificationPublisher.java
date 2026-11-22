package com.sai.notify.api.kafka;

import com.sai.notify.api.config.KafkaTopicConfig;
import com.sai.notify.api.domain.Priority;
import com.sai.notify.api.entity.NotificationEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j                      // Lombok: gives you a "log" variable for logging
@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private final KafkaTemplate<String, NotificationRequestedEvent> kafkaTemplate;

    public boolean publish(NotificationEntity n) {
        String topic = n.getPriority() == Priority.HIGH ? KafkaTopicConfig.REQUESTS_HIGH : KafkaTopicConfig.
                REQUESTS_LOW;


        NotificationRequestedEvent event = NotificationRequestedEvent.builder()
                .notificationId(n.getId())
                .category(n.getCategory())
                .expiresAt(n.getExpiresAt())
                .priority(n.getPriority())
                .templateId(n.getTemplateId())
                .userId(n.getUserId())
                .variables(n.getVariables())
                .build();

        String key = String.valueOf(n.getUserId());

        try {
            kafkaTemplate.send(topic, key, event).get(2, TimeUnit.SECONDS);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();   // keep the "please stop" signal
            log.warn("Publish interrupted for {}", n.getId());
            return false;
        } catch (Exception e) {
            log.warn("Publish failed for {}: {}", n.getId(), e.getMessage());
            return false;
        }
    }
}