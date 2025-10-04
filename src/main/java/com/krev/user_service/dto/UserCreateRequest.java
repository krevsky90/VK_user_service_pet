package com.krev.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(@NotBlank(message = "name is mandatory") @Size(max = 100) String name, @NotBlank @Email String email) {
}
