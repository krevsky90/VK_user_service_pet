package com.krev.user_service.service;

import com.krev.user_service.dto.UserCreateRequest;
import com.krev.user_service.dto.UserCreatedEvent;
import com.krev.user_service.dto.UserEvent;
import com.krev.user_service.dto.UserResponse;
import com.krev.user_service.exception.UserNotFoundException;
import com.krev.user_service.model.User;
import com.krev.user_service.repository.UserRepository;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;
    @Autowired
    private NewTopic topic;

    public UserResponse createUser(UserCreateRequest request) {
        User user = new User(request.name(), request.email());

        User savedUser = userRepository.save(user);
        UserResponse userResponse = new UserResponse(savedUser.getId(), savedUser.getName(), savedUser.getEmail());

        //send notification before we return the response
        UserCreatedEvent event = new UserCreatedEvent(savedUser.getId(), savedUser.getName(), savedUser.getEmail());

        //NOTE: we do this in async mode! + use whenComplete
        //NOTE: always use try catch!
        //todo: move to separate sender-class/bean (that can take topicName and kafkaTemplate as parameters)
        try {
            kafkaTemplate.send(topic.name(), event)
                    .whenComplete(
                            (result, ex) -> {
                                if (ex == null) {
                                    LOGGER.info("message id: {} is sent. offset: {}",
                                            event.userId(),
                                            result.getRecordMetadata().offset());
                                } else {
                                    LOGGER.error("message id: {} is NOT sent", event.userId(), ex);
                                }
                            }
                    );
        } catch (Exception ex) {
            LOGGER.error("send error, value {}", event, ex);
        }

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