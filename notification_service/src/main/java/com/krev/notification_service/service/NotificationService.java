package com.krev.notification_service.service;

import com.krev.notification_service.dto.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    public void handleUserCreated(UserEvent event) {
        LOGGER.info("Sending notification for new user {} (id: {}", event.name(), event.userId());
        LOGGER.info("Mock email sent to {}", event.email());
    }
}
