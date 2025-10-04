package com.krev.user_service.repository;

import com.krev.user_service.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    // Spring Data автоматически реализует CRUD
}
