package com.krev.user_service.dto;

public record UserEvent(String userId, String name, String email, String eventType) {
}