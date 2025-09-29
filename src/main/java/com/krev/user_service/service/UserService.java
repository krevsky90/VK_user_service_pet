package com.krev.user_service.service;

import com.krev.user_service.dao.UserDao;
import com.krev.user_service.dao.UserEntity;
import com.krev.user_service.dto.UserCreateRequest;
import com.krev.user_service.dto.UserResponse;
import com.krev.user_service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserDao userDao;

    private final AtomicLong idGenerator = new AtomicLong();

    public UserResponse createUser(UserCreateRequest request) {
        Long id = idGenerator.getAndIncrement();
        UserEntity userEntity = new UserEntity(id, request.name(), request.email());

        userDao.save(userEntity);
        return new UserResponse(id, userEntity.name(), userEntity.email());
    }

    public UserResponse findUserById(Long id) {
        Optional<UserEntity> userOpt = userDao.findById(id);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("User with id " + id + " not found");
        }
        UserEntity user = userOpt.get();
        return new UserResponse(id, user.name(), user.email());
    }

    public List<UserResponse> getAllUsers() {
        List<UserEntity> users = userDao.getAll();

        return users.stream()
                .map(user -> new UserResponse(user.id(), user.name(), user.email()))
                .collect(Collectors.toList());
    }
}