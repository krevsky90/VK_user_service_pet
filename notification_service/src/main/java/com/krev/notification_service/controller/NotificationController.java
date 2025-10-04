package com.krev.notification_service.controller;

import com.krev.notification_service.dto.UserEvent;
import com.krev.notification_service.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notify")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @PostMapping("/user-created")
    public ResponseEntity<Void> handleUserCreated(@Valid @RequestBody UserEvent event) {
        notificationService.handleUserCreated(event);
        return ResponseEntity.accepted().build();

    }
}
