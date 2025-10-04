package com.krev.notification_service.dto;

public record UserEvent(String userId, String name, String email, String eventType) {
}