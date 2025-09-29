package com.krev.user_service.dao;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class UserDao {//implements IDao<UserEntity> {
    private final Map<Long, UserEntity> map = new ConcurrentHashMap<>();

//    @Override
    public void save(UserEntity userEntity) {
        map.put(userEntity.id(), userEntity);
    }

//    @Override
    public Optional<UserEntity> findById(Long id) {
        return Optional.ofNullable(map.get(id));
    }

//    @Override
    public List<UserEntity> getAll() {
        return map.values().stream().collect(Collectors.toList());
    }
}