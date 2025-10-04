package com.krev.user_service.client;

import com.krev.user_service.dto.UserEvent;
import com.krev.user_service.dto.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class NotificationClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationClient.class);

    private final String notificationServiceUrl;
    private final RestTemplate restTemplate;

    //notificationServiceUrl will be taken from docker-compose file, from ENV variable NOTIFICATION_SERVICE_URL=http://notification-service:8081
    //that has specific format that will be converted (by internal DNS) from NOTIFICATION_SERVICE_URL to notification.service.url
    public NotificationClient(@Value("${notification.service.url:http://localhost:8081}") String notificationServiceUrl, RestTemplate restTemplate) {
        this.notificationServiceUrl = notificationServiceUrl;
        this.restTemplate = restTemplate;
    }

    public void notifyUserCreated(UserResponse userResponse) {
        //here we use UserEvent class which is copied from notification_service module
        UserEvent event = new UserEvent(
                userResponse.id(),
                userResponse.name(),
                userResponse.email(),
                "USER_CREATED"
        );

        String url = notificationServiceUrl + "/api/v1/notify/user-created";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // RestTemplate сам сериализует event в JSON через Jackson
        // Spring Boot автоматически настраивает RestTemplate с MappingJackson2HttpMessageConverter
        // При отправке объекта UserEvent он автоматически сериализуется в JSO
        HttpEntity<UserEvent> request = new HttpEntity<>(event, headers);

        try {
            restTemplate.postForEntity(url, request, Void.class);
            LOGGER.info("Notification sent for user {}", userResponse.id());
        } catch (RestClientException ex) {
            LOGGER.warn("Failed to send notification for user {}", userResponse.id(), ex);
        }
    }
}
