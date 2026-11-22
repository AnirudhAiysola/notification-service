package com.sai.notify.api.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String REQUESTS_HIGH = "notifications.requests.high";
    public static final String REQUESTS_LOW  = "notifications.requests.low";

    // Spring creates these topics on startup if they don't exist yet
    @Bean
    NewTopic requestsHigh() {
        return TopicBuilder.name(REQUESTS_HIGH).partitions(12).replicas(1).build();
    }

    @Bean
    NewTopic requestsLow() {
        return TopicBuilder.name(REQUESTS_LOW).partitions(12).replicas(1).build();
    }
}