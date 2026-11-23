package com.sai.notify.api.repository;

import com.sai.notify.api.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    Optional<NotificationEntity> findByIdempotencyKey(String idempotencyKey);

    @Modifying
    @Transactional
    @Query("UPDATE NotificationEntity n SET n.publishedAt = :at WHERE n.id = :id")
    int markPublished(@Param("id") UUID id, @Param("at") Instant at);

    @Query(value = """
        SELECT * FROM notifications
        WHERE published_at IS NULL
        ORDER BY created_at
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<NotificationEntity> findUnpublishedForUpdate(@Param("limit") int limit);
}