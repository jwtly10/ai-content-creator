package com.jwtly10.aicontentgenerator.repository;

import java.util.List;
import java.util.Optional;

public interface UserDAO<T> {
    List<T> list();

    void create(T t);

    Optional<T> get(String email);

    int update(T t, int id);

    int delete(int id);
}
