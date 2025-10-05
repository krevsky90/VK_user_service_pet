package com.krev.user_service.dto;

public record UserCreatedEvent(String userId, String name, String email) {
}
