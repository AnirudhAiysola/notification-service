package com.sai.notify.api.kafka;

import com.sai.notify.api.entity.NotificationEntity;
import com.sai.notify.api.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private static final int BATCH_SIZE = 100;

    private final NotificationRepository repository;
    private final NotificationPublisher publisher;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPending() {

        List<NotificationEntity> unpublishedRows = repository.findUnpublishedForUpdate(BATCH_SIZE);

        if(unpublishedRows.isEmpty()) return;

        int published = 0;
        for(NotificationEntity row : unpublishedRows){
            if(publisher.publish(row)){
                repository.markPublished(row.getId(),Instant.now());
            }else{
                break;
            }
        }

        log.info("Outbox: published {}/{}", published, unpublishedRows.size());
    }
}