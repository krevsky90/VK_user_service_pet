package com.krev.user_service.service;

import com.krev.user_service.client.NotificationClient;
import com.krev.user_service.dto.UserCreateRequest;
import com.krev.user_service.dto.UserResponse;
import com.krev.user_service.exception.UserNotFoundException;
import com.krev.user_service.model.User;
import com.krev.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationClient notificationClient;

    public UserResponse createUser(UserCreateRequest request) {
        User user = new User(request.name(), request.email());

        User savedUser = userRepository.save(user);
        UserResponse userResponse = new UserResponse(savedUser.getId(), savedUser.getName(), savedUser.getEmail());

        //send notification before we return the responce
        notificationClient.notifyUserCreated(userResponse);

        return userResponse;
    }

    public UserResponse findUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
        return new UserResponse(id, user.getName(), user.getEmail());
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> new UserResponse(user.getId(), user.getName(), user.getEmail()))
                .collect(Collectors.toList());
    }
}