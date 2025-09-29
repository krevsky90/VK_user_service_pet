package com.krev.user_service.dao;

import java.util.List;
import java.util.Optional;

public interface IDao<E> {
    void save(E e);

    Optional<E> findById(Long id);

    Optional<List<E>> getAll();
}
