package com.sai.notify.api.service;

import com.sai.notify.api.domain.Priority;
import com.sai.notify.api.dto.NotificationRequest;
import com.sai.notify.api.entity.NotificationEntity;
import com.sai.notify.api.kafka.NotificationPublisher;
import com.sai.notify.api.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Duration HIGH_DEFAULT_TTL = Duration.ofMinutes(5);
    private static final Duration LOW_DEFAULT_TTL  = Duration.ofHours(24);

    private final NotificationRepository repository;
    private final NotificationPublisher publisher;


    /**
     * Saves the request once. Returns the notification id.
     * Same idempotencyKey again → returns the ORIGINAL id, saves nothing new.
     */
    public UUID accept(NotificationRequest request) {
        Optional<NotificationEntity> key = repository.findByIdempotencyKey(request.idempotencyKey());
        if(key.isPresent()){
            return key.get().getId();
        }

        Duration ttl = null;
        if(request.ttlSeconds() != null){
            ttl = Duration.ofSeconds(request.ttlSeconds());
        }else{
            ttl = request.priority() == Priority.HIGH ? HIGH_DEFAULT_TTL : LOW_DEFAULT_TTL;
        }

        Instant now = Instant.now();
        NotificationEntity entity = NotificationEntity.builder()
                .id(UUID.randomUUID())
                .idempotencyKey(request.idempotencyKey())
                .priority(request.priority())
                .category(request.category())
                .templateId(request.templateId())
                .createdAt(now)
                .expiresAt(now.plus(ttl))
                .variables(request.variables())
                .userId(request.userId())
                .build();

           try {
               NotificationEntity saved = repository.saveAndFlush(entity);
               if(publisher.publish(saved)){
                   repository.markPublished(saved.getId(), now);
               }
               return saved.getId();
           } catch (DataIntegrityViolationException e) {
               return repository.findByIdempotencyKey(request.idempotencyKey())
                       .orElseThrow(() -> e)
                       .getId();
           }
    }
}