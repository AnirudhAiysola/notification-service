package com.sai.notify.api.service;

import com.sai.notify.api.domain.Category;
import com.sai.notify.api.domain.Priority;
import com.sai.notify.api.dto.NotificationRequest;
import com.sai.notify.api.entity.NotificationEntity;
import com.sai.notify.api.kafka.NotificationPublisher;
import com.sai.notify.api.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(properties = "spring.kafka.admin.auto-create=false") // don't try to create topics in tests
class NotificationServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    NotificationService service;

    @Autowired
    NotificationRepository repository;

    @MockitoBean
    NotificationPublisher publisher;   // fake Kafka

    @BeforeEach
    void setUp() {
        repository.deleteAll();                                   // clean table per test
        when(publisher.publish(any())).thenReturn(true);          // "Kafka" always succeeds
    }

    // Helper: builds a request with the given key and priority
    private NotificationRequest request(String key, Priority priority) {
        return new NotificationRequest(key, 42L, "order_shipped", priority,
                Category.TRANSACTIONAL, null, null);
    }

    @Test
    void sameKeyTwice_returnsSameId_andStoresOneRow() {
        UUID id1 = service.accept(request("dup-key", Priority.LOW));
        UUID id2 = service.accept(request("dup-key", Priority.LOW));

        assertThat(id2).isEqualTo(id1);
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void highPriority_withoutTtl_expiresInAbout5Minutes() {
        UUID id = service.accept(request("otp-key", Priority.HIGH));

        NotificationEntity row = repository.findById(id).orElseThrow();
        Duration gap = Duration.between(row.getCreatedAt(), row.getExpiresAt());

        assertThat(gap).isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void newRequest_isMarkedPublished() {
        UUID id = service.accept(request("pub-key", Priority.LOW));

        NotificationEntity row = repository.findById(id).orElseThrow();

        assertThat(row.getPublishedAt()).isNotNull();
    }

    @Test
    void twoIdenticalRequestsAtTheSameTime_produceOneRow() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch startLine = new CountDownLatch(1);
        NotificationRequest req = request("race-key", Priority.LOW);

        Callable<UUID> task = () -> {
            startLine.await();
            return service.accept(req);
        };

        Future<UUID> f1 = pool.submit(task);
        Future<UUID> f2 = pool.submit(task);

        startLine.countDown();   // release both threads at once

        UUID id1 = f1.get(5, TimeUnit.SECONDS);
        UUID id2 = f2.get(5, TimeUnit.SECONDS);

        assertThat(id2).isEqualTo(id1);
        assertThat(repository.count()).isEqualTo(1);

        pool.shutdown();
    }
    }
