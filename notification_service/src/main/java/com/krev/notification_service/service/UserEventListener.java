package com.krev.notification_service.service;

import com.krev.notification_service.dto.UserEvent;
import com.krev.user_service.dto.UserCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserEventListener.class);

    @KafkaListener(topics = "${application.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleUserCreated(@Payload List<UserCreatedEvent> events) {
        for (UserCreatedEvent event : events) {
            LOGGER.info("Received user.created event for user: {} (ID: {})", event.name(), event.userId());
            LOGGER.info("Mock email sent to {}", event.email());
        }
    }
}